package com.energyict.mdc.engine.impl.core;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.protocol.api.security.DeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.SecurityProperty;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySetImpl;
import com.energyict.mdc.tasks.BasicCheckTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdw.core.CommunicationDevice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public static List<DeviceOrganizedComTaskExecution> defineComTaskExecutionOrders(ComTaskExecution... comTaskExecutions) {
        List<DeviceOrganizedComTaskExecution> deviceOrganizedComTaskExecutions = new ArrayList<>();
        Map<Device, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice = groupComTaskExecutionsByDevice(Arrays.asList(comTaskExecutions));
        makeSureBasicCheckIsInBeginningOfExecutions(comTaskExecutionGroupsPerDevice);
        Map<Device, List<ComTaskExecutionsPerSecurityPropertySet>> comTaskExecutionGroupsPerDevicePerSecuritySet =
                groupComTaskExecutionsBySecuritySet(comTaskExecutionGroupsPerDevice);

        int counter = 0;
        for (Map.Entry<Device, List<ComTaskExecutionsPerSecurityPropertySet>> deviceListEntry : comTaskExecutionGroupsPerDevicePerSecuritySet.entrySet()) {
            boolean firstDevice = (counter == 0);
            boolean lastDevice = (counter++ == (comTaskExecutionGroupsPerDevicePerSecuritySet.size() - 1));
            DeviceOrganizedComTaskExecution deviceOrganizedComTaskExecution = new DeviceOrganizedComTaskExecution(deviceListEntry.getKey());
            int securityCounter = 0;
            final List<ComTaskExecutionsPerSecurityPropertySet> comTaskExecutionsPerSecuritySetForDevice = deviceListEntry.getValue();
            for (ComTaskExecutionsPerSecurityPropertySet comTaskExecutionsPerSecurityPropertySet : comTaskExecutionsPerSecuritySetForDevice) {
                boolean firstSecuritySet = (securityCounter == 0);
                boolean lastSecuritySet = (securityCounter++ == (comTaskExecutionsPerSecuritySetForDevice.size() - 1));
                DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet =
                        getDeviceProtocolSecurityPropertySet(comTaskExecutionsPerSecurityPropertySet.getSecurityPropertySet(), deviceListEntry.getKey());
                final List<ComTaskExecution> comTaskExecutionsForSet = comTaskExecutionsPerSecurityPropertySet.getComTaskExecutions();
                for (int comTaskListIndex = 0; comTaskListIndex < comTaskExecutionsForSet.size(); comTaskListIndex++) {
                    final ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps =
                            defineComTaskExecutionOrder(comTaskListIndex, comTaskExecutionsForSet.size(), firstDevice, lastDevice, firstSecuritySet, lastSecuritySet);
                    deviceOrganizedComTaskExecution.addComTaskWithConnectionSteps(comTaskExecutionsForSet.get(comTaskListIndex), comTaskExecutionConnectionSteps, deviceProtocolSecurityPropertySet);
                }
            }
            deviceOrganizedComTaskExecutions.add(deviceOrganizedComTaskExecution);
        }
        return deviceOrganizedComTaskExecutions;
    }

    private static DeviceProtocolSecurityPropertySet getDeviceProtocolSecurityPropertySet(SecurityPropertySet securityPropertySet, Device masterDevice) {
        if (securityPropertySet != null) {
                /* The actual retrieving of the properties must be done on the given masterDevice */
//            List<SecurityProperty> protocolSecurityProperties = ((CommunicationDevice) masterDevice).getProtocolSecurityProperties(securityPropertySet);
            List<SecurityProperty> protocolSecurityProperties = ManagerFactory.getCurrent().getSecurityPropertyFactory().getSecurityProperties(new Date(), masterDevice, securityPropertySet);
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

    private static ComTaskExecutionConnectionSteps defineComTaskExecutionOrder(int listIndex, int listSize, boolean firstDevice, boolean lastDevice, boolean firstOfSecuritySet, boolean lastOfSecuritySet) {
        ComTaskExecutionConnectionSteps comTaskExecutionConnectionSteps = new ComTaskExecutionConnectionSteps();
        if (firstDevice && firstOfSecuritySet && listIndex == 0) {
            comTaskExecutionConnectionSteps.addFlag(ComTaskExecutionConnectionSteps.FIRST_OF_CONNECTION_SERIES);
        } else if (listIndex == 0) {
            comTaskExecutionConnectionSteps.addFlag(ComTaskExecutionConnectionSteps.FIRST_OF_SAME_CONNECTION_BUT_NOT_FIRST_DEVICE);
        }

        if (lastDevice && lastOfSecuritySet && listIndex == listSize - 1) {
            comTaskExecutionConnectionSteps.addFlag(ComTaskExecutionConnectionSteps.LAST_OF_CONNECTION_SERIES);
        } else if (listIndex == listSize - 1) {
            comTaskExecutionConnectionSteps.addFlag(ComTaskExecutionConnectionSteps.LAST_OF_SAME_CONNECTION_BUT_NOT_LAST_DEVICE);
        }
        return comTaskExecutionConnectionSteps;
    }

    private static Map<Device, List<ComTaskExecutionsPerSecurityPropertySet>> groupComTaskExecutionsBySecuritySet(Map<Device, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        Map<Device, List<ComTaskExecutionsPerSecurityPropertySet>> comTaskExecutionsBySecuritySetAndDevice = new LinkedHashMap<>();
        for (Map.Entry<Device, List<ComTaskExecution>> entry : comTaskExecutionGroupsPerDevice.entrySet()) {
            final List<ComTaskExecution> value = entry.getValue();
            Map<SecurityPropertySet, ComTaskExecutionsPerSecurityPropertySet> comTaskExecutionsBySecurityPropertySet = new LinkedHashMap<>();
            for (ComTaskExecution comTaskExecution : value) {
                mapComTaskExecutionToSecurityPropertySet(entry, comTaskExecutionsBySecurityPropertySet, comTaskExecution);
            }
            comTaskExecutionsBySecuritySetAndDevice.put(entry.getKey(), new ArrayList<>(comTaskExecutionsBySecurityPropertySet.values()));
        }
        return comTaskExecutionsBySecuritySetAndDevice;
    }

    private static void mapComTaskExecutionToSecurityPropertySet(Map.Entry<Device, List<ComTaskExecution>> entry, Map<SecurityPropertySet, ComTaskExecutionsPerSecurityPropertySet> comTaskExecutionsBySecurityPropertySet, ComTaskExecution comTaskExecution) {
    /* We look at the device which owns the ComTaskExecution to know which set we need to retrieve */
        Device device = comTaskExecution.getDevice();
        DeviceCommunicationConfiguration communicationConfiguration = device.getDeviceConfiguration().getCommunicationConfiguration();
        ComTaskEnablement comTaskEnablement = ManagerFactory.getCurrent().getComTaskEnablementFactory().findByDeviceCommunicationConfigurationAndComTask(communicationConfiguration, comTaskExecution.getComTask());
        SecurityPropertySet securityPropertySet = getProperSecurityPropertySet(comTaskEnablement, entry.getKey(), comTaskExecution);
        if (!comTaskExecutionsBySecurityPropertySet.containsKey(securityPropertySet)) {
            comTaskExecutionsBySecurityPropertySet.put(securityPropertySet, new ComTaskExecutionsPerSecurityPropertySet(securityPropertySet));
        }
        comTaskExecutionsBySecurityPropertySet.get(securityPropertySet).addComTaskExecution(comTaskExecution);
    }

    private static SecurityPropertySet getProperSecurityPropertySet(ComTaskEnablement comTaskEnablement, Device masterDevice, ComTaskExecution comTaskExecution) {
        if (comTaskEnablement != null) {
            final SecurityPropertySet securityPropertySet = comTaskEnablement.getSecurityPropertySet();
            if (masterDevice.equals(comTaskExecution.getDevice())) {
                return securityPropertySet;
            } else {
            /* In the exception case where the masterDevice is different then the device on the ComTaskEnablement
            * then we need to search for the corresponding securitySet of the master */
                final List<SecurityPropertySet> securityPropertySets = ManagerFactory.getCurrent().getSecurityPropertySetFactory().find(masterDevice.getDeviceConfiguration().getCommunicationConfiguration());
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

    private static void makeSureBasicCheckIsInBeginningOfExecutions(Map<Device, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice) {
        for (List<ComTaskExecution> comTaskExecutions : comTaskExecutionGroupsPerDevice.values()) {
            int basicCheckIndex = -1;
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                final List<? extends ProtocolTask> protocolTasks = comTaskExecution.getComTask().getProtocolTasks();
                for (ProtocolTask protocolTask : protocolTasks) {
                    if (BasicCheckTask.class.isAssignableFrom(protocolTask.getClass())) {
                        basicCheckIndex = comTaskExecutions.indexOf(comTaskExecution) + 1;
                    }
                }
            }
            if (basicCheckIndex != -1) {
                // this puts the ComTaskExecution with the basicCheckTask in the front of the list
                Collections.rotate(comTaskExecutions.subList(0, basicCheckIndex), 1);
            }
        }
    }

    private static Map<Device, List<ComTaskExecution>> groupComTaskExecutionsByDevice(List<? extends ComTaskExecution> comTaskExecutions) {
        Map<Device, List<ComTaskExecution>> comTaskExecutionGroupsPerDevice = new LinkedHashMap<>();
        for (ComTaskExecution comTaskExecution : comTaskExecutions) {
            final Device masterDevice = getMasterDeviceIfAvailable(comTaskExecution.getDevice());
            if (!comTaskExecutionGroupsPerDevice.containsKey(masterDevice)) {
                comTaskExecutionGroupsPerDevice.put(masterDevice, new ArrayList<ComTaskExecution>());
            }
            comTaskExecutionGroupsPerDevice.get(masterDevice).add(comTaskExecution);
        }
        return comTaskExecutionGroupsPerDevice;
    }

    private static Device getMasterDeviceIfAvailable(Device device) {
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

    // Hide utility class constructor
    private ComTaskExecutionOrganizer() {
        super();
    }

    private static class ComTaskExecutionsPerSecurityPropertySet {

        private final SecurityPropertySet securityPropertySet;
        private List<ComTaskExecution> comTaskExecutions = new ArrayList<>();

        private ComTaskExecutionsPerSecurityPropertySet(SecurityPropertySet securityPropertySet) {
            this.securityPropertySet = securityPropertySet;
        }

        public void addComTaskExecution(final ComTaskExecution comTaskExecution) {
            this.comTaskExecutions.add(comTaskExecution);
        }

        private SecurityPropertySet getSecurityPropertySet() {
            return securityPropertySet;
        }

        private List<ComTaskExecution> getComTaskExecutions() {
            return comTaskExecutions;
        }
    }

}
