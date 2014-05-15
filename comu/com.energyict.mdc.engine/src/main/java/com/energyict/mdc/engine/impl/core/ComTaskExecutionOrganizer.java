package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.google.common.base.Optional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

/**
 * This class will try to group and organize the ComTaskExecutions for a specific connection.
 * The criteria are:
 * <ul>
 * <li>All ComTaskExecutions should be grouped by Master Device</li>
 * <li>The ComTaskExecutions should be grouped by used SecurityPropertySet</li>
 * <li>If a ComTaskExecution contains a BasicCheck protocolTask, then it should be the first member of the ComTaskExecution <i>set</i>
 * AND it should be the first <i>set</i> of the Device</li>
 * </ul>
 * <p/>
 * Copyrights EnergyICT
 * Date: 4/04/13
 * Time: 16:38
 */
public final class ComTaskExecutionOrganizer {
    /**
     * Represents a DeviceProtocolSecurityPropertySet which is does basically nothing.
     * The Encryption- and AuthenticationAccessLevel are define as NOT USED.
     * The TypedProperty object doesn't contain any properties
     */
    protected static final DeviceProtocolSecurityPropertySet EMPTY_DEVICE_PROTOCOL_SECURITY_PROPERTY_SET =
            new DeviceProtocolSecurityPropertySetImpl(
                    DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID,
                    DeviceAccessLevel.NOT_USED_DEVICE_ACCESS_LEVEL_ID,
                    TypedProperties.empty());

    private final DeviceConfigurationService deviceConfigurationService;

    public ComTaskExecutionOrganizer(DeviceConfigurationService deviceConfigurationService) {
        super();
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public List<DeviceOrganizedComTaskExecution> defineComTaskExecutionOrders(List<? extends ComTaskExecution> comTaskExecutions) {
        List<DeviceOrganizedComTaskExecution> result = new ArrayList<>();
        Map<Device, List<ComTaskExecution>> tasksPerDevice = groupComTaskExecutionsByDevice(comTaskExecutions);
        makeSureBasicCheckIsInBeginningOfExecutions(tasksPerDevice);
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
            DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = new DeviceOrganizedComTaskExecution(key.getDevice());
            DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet = getDeviceProtocolSecurityPropertySet(key.getSecurityPropertySet(), key.getDevice());
            for (ListIterator<ComTaskExecution> iterator = deviceListEntry.getValue().listIterator(); iterator.hasNext(); ) {
                ComTaskExecutionConnectionSteps flags = determineFlags(previous, key, iterator.hasNext() ? key : next);
                deviceOrganizedComTaskExecution.addComTaskWithConnectionSteps(iterator.next(), flags, deviceProtocolSecurityPropertySet);
                previous = key;
            }
            result.add(deviceOrganizedComTaskExecution);
        }
        return result;
    }

    private ComTaskExecutionConnectionSteps determineFlags(Key previous, Key current, Key next) {
        ComTaskExecutionConnectionSteps steps = new ComTaskExecutionConnectionSteps(0);
        if (previous == null || previous.getDevice().getId() != current.getDevice().getId()) {
            steps.signOn();
        } else if (previous.getSecurityPropertySet().getId() != current.getSecurityPropertySet().getId()) {
            steps.logOn();
        }
        if (next == null || next.getDevice().getId() != current.getDevice().getId()) {
            steps.signOff();
        } else if (next.getSecurityPropertySet().getId() != current.getSecurityPropertySet().getId()) {
            steps.logOff();
        }
        return steps;
    }

    private DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(SecurityPropertySet securityPropertySet, Device masterDevice) {
        if (securityPropertySet != null) {
                /* The actual retrieving of the properties must be done on the given masterDevice */
//            List<SecurityProperty> protocolSecurityProperties = ((CommunicationDevice) masterDevice).getProtocolSecurityProperties(securityPropertySet);
            List<SecurityProperty> protocolSecurityProperties = masterDevice.getSecurityProperties(securityPropertySet);
            TypedProperties securityProperties = TypedProperties.empty();
            for (SecurityProperty protocolSecurityProperty : protocolSecurityProperties) {
                securityProperties.setProperty(protocolSecurityProperty.getName(), protocolSecurityProperty.getValue());
            }
            return new DeviceProtocolSecurityPropertySetImpl(
                    securityPropertySet.getAuthenticationDeviceAccessLevel().getId(),
                    securityPropertySet.getEncryptionDeviceAccessLevel().getId(),
                    securityProperties);
        } else {
            return EMPTY_DEVICE_PROTOCOL_SECURITY_PROPERTY_SET;
        }
    }

    private Device getMasterDeviceIfAvailable(Device device) {
        if (device.getDeviceType().isLogicalSlave()) {
            final Device gateway = device.getPhysicalGateway();
            if (gateway == null) {
                return device;
            }
            else {
                return getMasterDeviceIfAvailable(gateway);
            }
        } else {
            return device;
        }
    }

    private SecurityPropertySet getProperSecurityPropertySet(ComTaskEnablement comTaskEnablement, Device masterDevice, ComTaskExecution comTaskExecution) {
        if (comTaskEnablement != null) {
            final SecurityPropertySet securityPropertySet = comTaskEnablement.getSecurityPropertySet();
            if (masterDevice.equals(comTaskExecution.getDevice())) {
                return securityPropertySet;
            } else {
            /* In the exception case where the masterDevice is different then the device on the ComTaskEnablement
            * then we need to search for the corresponding securitySet of the master */
                final List<SecurityPropertySet> securityPropertySets = masterDevice.getDeviceConfiguration().getSecurityPropertySets();
                for (SecurityPropertySet propertySet : securityPropertySets) {
                    if (propertySet.getAuthenticationDeviceAccessLevel().getId() == securityPropertySet.getAuthenticationDeviceAccessLevel().getId()
                            && propertySet.getEncryptionDeviceAccessLevel().getId() == securityPropertySet.getEncryptionDeviceAccessLevel().getId()) {
                        return propertySet;
                    }
                }
                return null;
            }
        } else {
            return null;
        }
    }

    private Map<Device, List<ComTaskExecution>> groupComTaskExecutionsByDevice(List<? extends ComTaskExecution> comTaskExecutions) {
        Map<Device, List<ComTaskExecution>> result = new LinkedHashMap<>();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            final Device masterDevice = getMasterDeviceIfAvailable(comTaskExecution.getDevice());
            if (!result.containsKey(masterDevice)) {
                result.put(masterDevice, new ArrayList<ComTaskExecution>());
            }
            result.get(masterDevice).add(comTaskExecution);
        }
        return result;
    }

    private Map<Key, List<ComTaskExecution>> groupComTaskExecutionsBySecuritySet(Map<Device, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        Map<Key, List<ComTaskExecution>> result = new LinkedHashMap<>();
        for (Map.Entry<Device, List<ComTaskExecution>> entry : comTaskExecutionGroupsPerDevice.entrySet()) {
            Device device = entry.getKey();
            for (ComTaskExecution comTaskExecution : entry.getValue()) {
                Key key = toKey(device, comTaskExecution);
                if (!result.containsKey(key)) {
                    result.put(key, new ArrayList<ComTaskExecution>());
                }
                result.get(key).add(comTaskExecution);
            }
        }
        return result;
    }

    private void makeSureBasicCheckIsInBeginningOfExecutions(Map<Device, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        for (List<ComTaskExecution> comTaskExecutions : comTaskExecutionGroupsPerDevice.values()) {
            Collections.sort(comTaskExecutions, BasicCheckTasks.FIRST);
        }
    }

    private Key toKey(Device device, ComTaskExecution comTaskExecution) {
        Optional<ComTaskEnablement> foundComTaskEnablement = deviceConfigurationService.findComTaskEnablement(comTaskExecution.getComTask(), device.getDeviceConfiguration());
        SecurityPropertySet securityPropertySet = getProperSecurityPropertySet(foundComTaskEnablement.get(), device, comTaskExecution);
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
            for (ProtocolTask protocolTask : execution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof BasicCheckTask) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class Key {
        private final Device device;
        private final SecurityPropertySet securityPropertySet;

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
            return Objects.hash(device.getId(), securityPropertySet.getId());
        }

        private Key(Device device, SecurityPropertySet securityPropertySet) {
            this.device = device;
            this.securityPropertySet = securityPropertySet;
        }

        private boolean isSameDevice(Key that) {
            return device.getId() == that.device.getId();
        }

        private boolean isSameSecurityPropertySet(Key that) {
            return securityPropertySet.getId() == that.securityPropertySet.getId();
        }
    }
}
