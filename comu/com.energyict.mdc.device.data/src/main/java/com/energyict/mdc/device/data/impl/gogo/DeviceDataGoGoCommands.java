package com.energyict.mdc.device.data.impl.gogo;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides useful gogo commands that will support device related operations
 * that are NOT yet provided by the GUI.
 *
 * <ul>
 *   <li>enableOutboundCommunication
 *     <ul>
 *       <li>will add all outbound connections that are enabled on the configuration</li>
 *       <li>will schedule all communication tasks that are enabled on the configuration</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-06-06 (13:44)
 */
@Component(name = "com.energyict.mdc.device.data.gogo", service = DeviceDataGoGoCommands.class,
        property = {"osgi.command.scope=" + DeviceDataService.COMPONENTNAME, "osgi.command.function=enableOutboundCommunication"}, immediate = true)
public class DeviceDataGoGoCommands {

    private volatile TransactionService transactionService;
    private volatile DeviceDataService deviceDataService;

    private enum ScheduleFrequency {
        DAILY {
            @Override
            public void enableOutboundCommunication(String scheduleOption, List<Device> devices) {

            }
        },

        NONE {
            @Override
            public void enableOutboundCommunication(String scheduleOption, List<Device> deviceMRIDs) {
                // This enum value represents no scheduling frequency so we will not enable anything on the devices
            }
        };

        public static ScheduleFrequency fromString (String name) {
            try {
                return ScheduleFrequency.valueOf(name);
            }
            catch (IllegalArgumentException e) {
                return NONE;
            }
        }

        public abstract void enableOutboundCommunication(String scheduleOption, List<Device> devices);

    }

    public void enableOutboundCommunication (String scheduleFrequency, String scheduleOption, String... deviceMRIDs) {
        ScheduleFrequency.fromString(scheduleFrequency).enableOutboundCommunication(scheduleOption, this.findDevices(deviceMRIDs));
    }

    private List<Device> findDevices(String... deviceMRIDs) {
        List<Device> devices = new ArrayList<>(deviceMRIDs.length);
        for (String deviceMRID : deviceMRIDs) {
            this.addDeviceIfExists(deviceMRID, devices);
        }
        return devices;
    }

    private void addDeviceIfExists(String deviceMRID, List<Device> devices) {
        Device device = this.deviceDataService.findByUniqueMrid(deviceMRID);
        if (device != null) {
            devices.add(device);
        }
        else {
            System.out.println("Device with MRID " + deviceMRID + " does not exist and has been ignored");
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    private static class EnableDailyCommunicationTransaction {
        private final TransactionService transactionService;
        private final DeviceDataService deviceDataService;
        private final List<Device> devices;

        private EnableDailyCommunicationTransaction(TransactionService transactionService, DeviceDataService deviceDataService, List<Device> devices) {
            super();
            this.transactionService = transactionService;
            this.deviceDataService = deviceDataService;
            this.devices = devices;
        }

        private void execute () {
            for (Device device : this.devices) {
                this.execute(device);
            }
        }

        private void execute(final Device device) {
            this.transactionService.execute(new Transaction<Object>() {
                @Override
                public Object perform() {
                    ScheduledConnectionTask defaultConnectionTask = addScheduledConnectionTasks(device);
                    List<ComTaskExecution> comTaskExecutions = addComTaskExecutions(device);
                    device.save();
                    if (defaultConnectionTask != null) {
                        setDefaultConnectionTask(defaultConnectionTask, comTaskExecutions);
                    }
                    else {
                        System.out.println("No default connection task created for device " + device.getmRID() + "because no default was configured in the device configuration: " + device.getDeviceConfiguration().getName());
                    }
                    if (comTaskExecutions.isEmpty()) {
                        System.out.printf("No communication tasks were scheduled for device " + device.getmRID() + " because not tasks were enabled on the device configuration: " + device.getDeviceConfiguration().getName());
                    }
                    return null;
                }
            });
        }

        private ScheduledConnectionTask addScheduledConnectionTasks(Device device) {
            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            ScheduledConnectionTask defaultConnectionTask = null;
            List<PartialScheduledConnectionTask> partialOutboundConnectionTasks = deviceConfiguration.getPartialOutboundConnectionTasks();
            for (PartialScheduledConnectionTask partialOutboundConnectionTask : partialOutboundConnectionTasks) {
                ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(partialOutboundConnectionTask).add();
                if (partialOutboundConnectionTask.isDefault()) {
                    defaultConnectionTask = scheduledConnectionTask;
                }
            }
            return defaultConnectionTask;
        }

        private List<ComTaskExecution> addComTaskExecutions(Device device) {
            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            List<ComTaskExecution> comTaskExecutions = new ArrayList<>();
            for (ComTaskEnablement comTaskEnablement : deviceConfiguration.getComTaskEnablements()) {
                comTaskExecutions.add(device.getComTaskExecutionBuilder(comTaskEnablement).add());
            }
            return comTaskExecutions;
        }

        private void setDefaultConnectionTask(ScheduledConnectionTask defaultConnectionTask, List<ComTaskExecution> comTaskExecutions) {
            Device device = defaultConnectionTask.getDevice();
            deviceDataService.setDefaultConnectionTask(defaultConnectionTask);
            for (ComTaskExecution comTaskExecution : comTaskExecutions) {
                device.getComTaskExecutionUpdater(comTaskExecution).setUseDefaultConnectionTask(defaultConnectionTask).update();
                comTaskExecution.updateNextExecutionTimestamp();
                comTaskExecution.scheduleNow();
            }
        }

    }
}