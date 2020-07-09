/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.security.DeviceAccessLevel;
import com.energyict.mdc.common.tasks.BasicCheckTask;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.MessagesTask;
import com.energyict.mdc.common.tasks.ProtocolTask;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import com.energyict.protocol.exceptions.DeviceConfigurationException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ComTaskExecutionOrganizer {

    private final TopologyService topologyService;

    public ComTaskExecutionOrganizer(TopologyService topologyService) {
        super();
        this.topologyService = topologyService;
    }

    public List<DeviceOrganizedComTaskExecution> defineComTaskExecutionOrders(List<ComTaskExecution> comTaskExecutions) {
        Map<Device, DeviceOrganizedComTaskExecution> result = new LinkedHashMap<>();
        Map<DeviceKey, List<ComTaskExecution>> tasksPerDevice = groupComTaskExecutionsByDevice(comTaskExecutions);
        moveBasicCheckToHead(tasksPerDevice);
        moveCommandsToTail(tasksPerDevice);
        Map<Key, List<ComTaskExecution>> tasksPerDeviceAndSecurity = groupComTaskExecutionsBySecuritySet(tasksPerDevice);

        Key previous = null;
        Key next;
        Iterator<Map.Entry<Key, List<ComTaskExecution>>> peekNext = tasksPerDeviceAndSecurity.entrySet().iterator();
        if (peekNext.hasNext()) {
            peekNext.next();
        }
        for (Map.Entry<Key, List<ComTaskExecution>> deviceListEntry : tasksPerDeviceAndSecurity.entrySet()) {
            next = peekNext.hasNext() ? peekNext.next().getKey() : null;
            Key key = deviceListEntry.getKey();
            DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = this.getDeviceOrganizedComTaskExecution(result, key);
            DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = getDeviceProtocolSecurityPropertySet(key.getSecurityPropertySet(), key.getDevice());
            for (ListIterator<ComTaskExecution> iterator = deviceListEntry.getValue().listIterator(); iterator.hasNext(); ) {
                ComTaskExecution comTaskExecution = iterator.next();
                ComTaskExecutionConnectionSteps flags = determineFlags(previous, key, iterator.hasNext() ? key : next);
                deviceOrganizedComTaskExecution.addComTaskWithConnectionSteps(comTaskExecution, flags, deviceProtocolSecurityPropertySet);
                previous = key;
            }
        }
        return new ArrayList<>(result.values());
    }

    private DeviceOrganizedComTaskExecution getDeviceOrganizedComTaskExecution(Map<Device, DeviceOrganizedComTaskExecution> result, Key key) {
        DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution;
        if (result.containsKey(key.getDevice())) {
            deviceOrganizedComTaskExecution = result.get(key.getDevice());
        } else {
            deviceOrganizedComTaskExecution = new DeviceOrganizedComTaskExecution(key.getDevice());
            result.put(key.getDevice(), deviceOrganizedComTaskExecution);
        }
        return deviceOrganizedComTaskExecution;
    }

    private ComTaskExecutionConnectionSteps determineFlags(Key previous, Key current, Key next) {
        ComTaskExecutionConnectionSteps steps = new ComTaskExecutionConnectionSteps(0);
        if (previous == null) {
            steps.signOn();
        } else if (!previous.isSameSecurityPropertySet(current) || !previous.isSameDevice(current)) {
            steps.logOn();
        }

        if (next == null) {
            steps.signOff();
        } else if (!next.isSameSecurityPropertySet(current) || !next.isSameDevice(current)) {
            steps.logOff();
        }
        return steps;
    }

    private DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(SecurityPropertySet securityPropertySet, Device masterDevice) {
        Device device  = findMatchingDevice(securityPropertySet, masterDevice);
        return new DeviceProtocolSecurityPropertySetImpl(
                securityPropertySet.getName(),
                securityPropertySet.getClient(),
                securityPropertySet.getAuthenticationDeviceAccessLevel().getId(),
                securityPropertySet.getEncryptionDeviceAccessLevel().getId(),
                securityPropertySet.getSecuritySuite() != null ? securityPropertySet.getSecuritySuite().getId() : -1,
                securityPropertySet.getRequestSecurityLevel() != null ? securityPropertySet.getRequestSecurityLevel().getId() : -1,
                securityPropertySet.getResponseSecurityLevel() != null ? securityPropertySet.getResponseSecurityLevel().getId() : -1,
                device.getSecurityProperties(securityPropertySet));
    }

    private Device findMatchingDevice(SecurityPropertySet securityPropertySet, Device masterDevice) {
        if (securityPropertySetBelongsToDevice(securityPropertySet, masterDevice)) {
            return masterDevice;
        }

        List<Device> logicalSlaveDevices = topologyService.findPhysicalConnectedDevices(masterDevice)
                .stream()
                .filter(downstreamDevice -> downstreamDevice.getDeviceType().isLogicalSlave())
                .collect(Collectors.toList());
        return logicalSlaveDevices.stream()
                .filter(device -> securityPropertySetBelongsToDevice(securityPropertySet, device))
                .findAny()
                .orElseThrow(() -> new DeviceConfigurationException(MessageSeeds.FAILED_TO_FETCH_DEVICE_OWNING_SECURITY_PROPERTY_SET, securityPropertySet.getName()));
    }

    private boolean securityPropertySetBelongsToDevice(SecurityPropertySet securityPropertySet, Device device) {
        return device.getDeviceConfiguration().getSecurityPropertySets()
                .stream()
                .anyMatch(set -> set.getId() == securityPropertySet.getId());
    }

    private Device getMasterDeviceIfAvailable(Device device) {
        if (device.getDeviceType().isLogicalSlave()) {
            Optional<Device> gateway = this.topologyService.getPhysicalGateway(device);
            if (gateway.isPresent()) {
                return getMasterDeviceIfAvailable(gateway.get());
            } else {
                return device;
            }
        } else {
            return device;
        }
    }

    private Optional<SecurityPropertySet> getProperSecurityPropertySet(ComTaskEnablement comTaskEnablement, Device masterDevice, ComTaskExecution comTaskExecution) {
        if (comTaskEnablement != null) {
            final SecurityPropertySet securityPropertySet = comTaskEnablement.getSecurityPropertySet();
            if (masterDevice.getId() == comTaskExecution.getDevice().getId()) {
                return Optional.of(securityPropertySet);
            } else {
                /* In the exception case where the masterDevice is different then the device on the ComTaskExecution
                 * then we need to search for the corresponding securitySet on the master */
                final List<SecurityPropertySet> securityPropertySets = masterDevice.getDeviceConfiguration().getSecurityPropertySets();
                for (SecurityPropertySet masterPropertySet : securityPropertySets) {
                    if ((masterPropertySet.getAuthenticationDeviceAccessLevel().getId() == securityPropertySet.getAuthenticationDeviceAccessLevel().getId() || securityPropertySet.getAuthenticationDeviceAccessLevel().getId() == DeviceAccessLevel.CAN_INHERIT_PROPERTIES_FROM_MASTER_ID)
                            && masterPropertySet.getEncryptionDeviceAccessLevel().getId() == securityPropertySet.getEncryptionDeviceAccessLevel().getId() || securityPropertySet.getEncryptionDeviceAccessLevel().getId() == DeviceAccessLevel.CAN_INHERIT_PROPERTIES_FROM_MASTER_ID) {
                        return Optional.of(masterPropertySet);
                    }
                }
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    private Map<DeviceKey, List<ComTaskExecution>> groupComTaskExecutionsByDevice(List<? extends ComTaskExecution> comTaskExecutions) {
        Map<DeviceKey, List<ComTaskExecution>> result = new LinkedHashMap<>();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            final Device masterDevice = getMasterDeviceIfAvailable(comTaskExecution.getDevice());
            DeviceKey deviceKey = DeviceKey.of(masterDevice);
            if (!result.containsKey(deviceKey)) {
                result.put(deviceKey, new ArrayList<>());
            }
            result.get(deviceKey).add(comTaskExecution);
        }
        return result;
    }

    private Map<Key, List<ComTaskExecution>> groupComTaskExecutionsBySecuritySet(Map<DeviceKey, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        Map<Key, List<ComTaskExecution>> result = new LinkedHashMap<>();
        for (Map.Entry<DeviceKey, List<ComTaskExecution>> entry : comTaskExecutionGroupsPerDevice.entrySet()) {
            Device device = entry.getKey().device;
            for (ComTaskExecution comTaskExecution : entry.getValue()) {
                /* ScheduledComTaskExecutions:
                 *   1. have at least one ComTasks (so get(0) is not returning null)
                 *   2. all ComTasks in the ComSchedule must use the same SecurityPropertySet
                 * Therefore, it suffices to take the first ComTask. */
                ComTask comTask = comTaskExecution.getComTask();
                Key key = toKey(device, comTaskExecution, comTask);
                if (!result.containsKey(key)) {
                    result.put(key, new ArrayList<>());
                }
                result.get(key).add(comTaskExecution);
            }
        }
        return result;
    }

    private void moveBasicCheckToHead(Map<DeviceKey, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        for (List<ComTaskExecution> comTaskExecutions : comTaskExecutionGroupsPerDevice.values()) {
            comTaskExecutions.sort(BasicCheckTasks.FIRST);
        }
    }

    private void moveCommandsToTail(Map<DeviceKey, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        for (List<ComTaskExecution> comTaskExecutions : comTaskExecutionGroupsPerDevice.values()) {
            comTaskExecutions.sort(MessagesTasks.LAST);
        }
    }

    private Key toKey(Device device, ComTaskExecution comTaskExecution, ComTask comTask) {
        Optional<ComTaskEnablement> foundComTaskEnablement =
                comTaskExecution.getDevice().getDeviceConfiguration().getComTaskEnablementFor(comTask);
        SecurityPropertySet securityPropertySet =
                foundComTaskEnablement
                        .flatMap(cte -> this.getProperSecurityPropertySet(cte, device, comTaskExecution))
                        .orElseThrow(() -> new DeviceConfigurationException(MessageSeeds.COMTASK_NOT_ENABLED_ON_CONFIGURATION, comTask.getName(), device.getDeviceConfiguration().getName()));
        return Key.of(device, securityPropertySet);
    }

    enum BasicCheckTasks implements Comparator<ComTaskExecution> {
        FIRST;

        @Override
        public int compare(ComTaskExecution o1, ComTaskExecution o2) {
            if (hasBasicCheckTask(o1)) {
                return hasBasicCheckTask(o2) ? 0 : -1;
            }
            return hasBasicCheckTask(o2) ? 1 : 0;
        }

        private boolean hasBasicCheckTask(ComTaskExecution execution) {
            for (ProtocolTask protocolTask : execution.getProtocolTasks()) {
                if (protocolTask instanceof BasicCheckTask) {
                    return true;
                }
            }
            return false;
        }
    }

    enum MessagesTasks implements Comparator<ComTaskExecution> {
        LAST;

        @Override
        public int compare(ComTaskExecution o1, ComTaskExecution o2) {
            if (hasMessagesTask(o1)) {
                return hasMessagesTask(o2) ? 0 : 1;
            }
            return hasMessagesTask(o2) ? -1 : 0;
        }

        private boolean hasMessagesTask(ComTaskExecution execution) {
            for (ProtocolTask protocolTask : execution.getProtocolTasks()) {
                if (protocolTask instanceof MessagesTask) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class DeviceKey {
        private final Device device;

        private DeviceKey(Device device) {
            this.device = device;
        }

        public static DeviceKey of(Device device) {
            return new DeviceKey(device);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DeviceKey)) {
                return false;
            }

            DeviceKey deviceKey = (DeviceKey) o;

            return device.getId() == deviceKey.device.getId();

        }

        @Override
        public int hashCode() {
            int result = 17;
            long deviceId = device.getId();
            result = 31 * result + (int) (deviceId ^ (deviceId >>> 32));
            return result;
        }
    }

    private static class Key {
        private final Device device;
        private final SecurityPropertySet securityPropertySet;

        private Key(Device device, SecurityPropertySet securityPropertySet) {
            this.device = device;
            this.securityPropertySet = securityPropertySet;
        }

        public static Key of(Device device, SecurityPropertySet securityPropertySet) {
            return new Key(device, securityPropertySet);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Key that = (Key) o;

            return isSameDevice(that) && isSameSecurityPropertySet(that);
        }

        public Device getDevice() {
            return device;
        }

        public SecurityPropertySet getSecurityPropertySet() {
            return securityPropertySet;
        }

        @Override
        public int hashCode() {
            long result = device.getId();
            result = 31 * result + securityPropertySet.getId();
            return (int) result;
        }

        private boolean isSameDevice(Key that) {
            return device.getId() == that.device.getId();
        }

        private boolean isSameSecurityPropertySet(Key that) {
            return securityPropertySet.getId() == that.securityPropertySet.getId();
        }

    }

}
