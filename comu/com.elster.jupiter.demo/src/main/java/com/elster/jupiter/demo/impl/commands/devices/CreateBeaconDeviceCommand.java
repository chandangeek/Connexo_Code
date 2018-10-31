/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.device.ConnectionsDevicePostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetCustomAttributeValuesToDevicePostBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.commands.AddLocationInfoToDevicesCommand;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CreateBeaconDeviceCommand {
    private static final List<DeviceTypeTpl> SPE_DEVICE_TYPES = Arrays.asList(DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Elster_A1800,
            DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38);

    private final DeviceService deviceService;
    private final SecurityManagementService securityManagementService;
    private final ConnectionTaskService connectionTaskService;
    private final Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider;
    private final Provider<ActivateDevicesCommand> activateDevicesCommandProvider;
    private final Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;
    private final Clock clock;

    private DeviceTypeTpl deviceTypeTpl;
    private DeviceConfiguration deviceConfiguration;
    private String serialNumber;
    private String deviceName;
    private String host = "158.138.16.171";
    private boolean withLocation;
    private KeyAccessorValuePersister keyAccessorValuePersister;
    static final String SECURITY_PROPERTY_SET_NAME = "No security";
    static final String SECURITY_ACCESSOR_TYPE = "PSK";

    @Inject
    public CreateBeaconDeviceCommand(DeviceService deviceService,
                                     SecurityManagementService securityManagementService,
                                     ConnectionTaskService connectionTaskService,
                                     Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider,
                                     Provider<ActivateDevicesCommand> activateDevicesCommandProvider,
                                     Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider,
                                     Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider,
                                     Clock clock) {
        this.deviceService = deviceService;
        this.securityManagementService = securityManagementService;
        this.connectionTaskService = connectionTaskService;
        this.connectionsDevicePostBuilderProvider = connectionsDevicePostBuilderProvider;
        this.activateDevicesCommandProvider = activateDevicesCommandProvider;
        this.setCustomAttributeValuesToDevicePostBuilderProvider = setCustomAttributeValuesToDevicePostBuilderProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
        this.clock = clock;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setDeviceTypeTpl(DeviceTypeTpl deviceTypeTpl) {
        this.deviceTypeTpl = deviceTypeTpl;
    }

    public void setDeviceTypeTpl(String deviceType) {
        if (deviceType != null) {
            this.deviceTypeTpl = SPE_DEVICE_TYPES.stream()
                    .filter(tpl -> tpl.getName().equals(deviceType))
                    .findFirst()
                    .orElseThrow(() -> new UnableToCreate("Can't find device type with name: " + deviceType));
        } else {
            this.deviceTypeTpl = null;
        }
    }

    public void setDeviceConfiguration(String deviceConfiguration) {
        if (deviceConfiguration != null) {
            if (this.deviceTypeTpl == null) {
                throw new UnableToCreate("Please specify the device type first");
            }
            this.deviceConfiguration = Builders.from(this.deviceTypeTpl).get()
                    .getConfigurations().stream()
                    .filter(configuration -> configuration.getName().equals(deviceConfiguration))
                    .findFirst()
                    .orElseThrow(() -> new UnableToCreate("Can't find device configuration with name: " + deviceConfiguration));
        } else {
            this.deviceConfiguration = null;
        }
    }

    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        if (this.deviceTypeTpl == null) {
            throw new UnableToCreate("Please specify the device type first");
        }
        this.deviceConfiguration = deviceConfiguration;
    }

    public void withLocation() {
        this.withLocation = true;
    }

    public String run() {
        if (this.serialNumber == null) {
            throw new UnableToCreate("Please specify the serial number for device");
        }
        if (this.deviceTypeTpl == null) {
            this.deviceTypeTpl = SPE_DEVICE_TYPES.get(new Random().nextInt(SPE_DEVICE_TYPES.size()));
        }
        if (this.deviceConfiguration == null) {
            List<DeviceConfiguration> configurations = Builders.from(this.deviceTypeTpl).get().getConfigurations();
            if (configurations.isEmpty()) {
                throw new UnableToCreate("This device type has no device configurations");
            }
            this.deviceConfiguration = configurations.get(new Random().nextInt(configurations.size()));
        }
        //String name = Constants.Device.BEACON_PREFIX + this.serialNumber;
        Device device = Builders.from(DeviceBuilder.class)
                .withName(deviceName)
                .withShippingDate(this.clock.instant().minus(30, ChronoUnit.DAYS))
                .withSerialNumber(this.serialNumber)
                .withDeviceConfiguration(this.deviceConfiguration)
                .withComSchedules(Collections.singletonList(Builders.from(ComScheduleTpl.DAILY_READ_ALL).get()))
                .withPostBuilder(this.connectionsDevicePostBuilderProvider.get()
                        .withComPortPool(Builders.from(deviceTypeTpl.getPoolTpl()).get())
                        .withHost(this.host))
                .withPostBuilder(this.setCustomAttributeValuesToDevicePostBuilderProvider.get())
                .get();


        addSecurityPropertiesToDevice(device);

        device.save();
        // addConnectionTasksToDevice(device);
        // add location
        AddLocationInfoToDevicesCommand addLocationInfoToDevicesCommand = this.addLocationInfoToDevicesCommandProvider.get();
        addLocationInfoToDevicesCommand.setDevices(Collections.singletonList(device));
        addLocationInfoToDevicesCommand.run();

        // activate
        ActivateDevicesCommand activateDevicesCommand = activateDevicesCommandProvider.get();
        activateDevicesCommand.setDevices(Collections.singletonList(device));
        activateDevicesCommand.run();
        return deviceName;
    }

    private void addConnectionTasksToDevice(Device device) {
        DeviceConfiguration configuration = device.getDeviceConfiguration();
        //PartialScheduledConnectionTask outboundConnectionTask = configuration.getPartialOutboundConnectionTasks().get(0);
        //ScheduledConnectionTask scheduledConnectionTask = device.getScheduledConnectionTaskBuilder(outboundConnectionTask).add();
        //connectionTaskService.setDefaultConnectionTask(scheduledConnectionTask);
        PartialInboundConnectionTask partialInboundConnectionTask = configuration.getPartialInboundConnectionTasks().get(0);
        InboundConnectionTask inboundConnectionTask = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask)
                .setConnectionTaskLifecycleStatus(ConnectionTask.ConnectionTaskLifecycleStatus.ACTIVE)
                .add();
        connectionTaskService.setConnectionTaskHavingConnectionFunction(inboundConnectionTask, Optional.empty());
    }

    private void addSecurityPropertiesToDevice(Device device) {
        getKeyAccessorValuePersister().persistKeyAccessorValue(device, "PSK", "3BEB4FFCA96E8869FA1F6E4BD78AB1D5");
    }

    private KeyAccessorValuePersister getKeyAccessorValuePersister() {
        if (keyAccessorValuePersister == null) {
            keyAccessorValuePersister = new KeyAccessorValuePersister(securityManagementService);
        }
        return keyAccessorValuePersister;
    }
}
