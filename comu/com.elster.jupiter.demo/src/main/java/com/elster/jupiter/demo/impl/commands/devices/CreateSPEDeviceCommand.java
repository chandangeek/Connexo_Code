/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.configuration.WebRTUNTASimultationToolPropertyPostBuilder;
import com.elster.jupiter.demo.impl.builders.device.ConnectionsDevicePostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SecurityPropertiesDevicePostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetCustomAttributeValuesToDevicePostBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.commands.AddLocationInfoToDevicesCommand;
import com.elster.jupiter.demo.impl.commands.CreateUsagePointsForDevicesCommand;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CreateSPEDeviceCommand {
    private static final List<DeviceTypeTpl> SPE_DEVICE_TYPES = Arrays.asList(DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Elster_A1800,
            DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38);

    private final DeviceService deviceService;
    private final Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider;
    private final Provider<ActivateDevicesCommand> activateDevicesCommandProvider;
    private final Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;
    private final Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider;

    private DeviceTypeTpl deviceTypeTpl;
    private DeviceConfiguration deviceConfiguration;
    private String serialNumber;
    private String host = "localhost";
    private boolean withUsagePoint;
    private boolean withLocation;
    private boolean shouldBeActive;

    @Inject
    public CreateSPEDeviceCommand(DeviceService deviceService,
                                  Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider,
                                  Provider<ActivateDevicesCommand> activateDevicesCommandProvider,
                                  Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider,
                                  Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider,
                                  Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider) {
        this.deviceService = deviceService;
        this.connectionsDevicePostBuilderProvider = connectionsDevicePostBuilderProvider;
        this.activateDevicesCommandProvider = activateDevicesCommandProvider;
        this.setCustomAttributeValuesToDevicePostBuilderProvider = setCustomAttributeValuesToDevicePostBuilderProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
        this.createUsagePointsForDevicesCommandProvider = createUsagePointsForDevicesCommandProvider;
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

    public void withUsagePoint() {
        this.withUsagePoint = true;
    }

    public void withLocation() {
        this.withLocation = true;
    }

    public void deviceShouldBeActive() {
        this.shouldBeActive = true;
    }

    public void run() {
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
        String name = Constants.Device.STANDARD_PREFIX + this.serialNumber;
        Device device = Builders.from(DeviceBuilder.class)
                .withName(name)
                .withSerialNumber(this.serialNumber)
                .withDeviceConfiguration(this.deviceConfiguration)
                .withComSchedules(Collections.singletonList(Builders.from(ComScheduleTpl.DAILY_READ_ALL).get()))
                .withPostBuilder(this.connectionsDevicePostBuilderProvider.get()
                        .withComPortPool(Builders.from(deviceTypeTpl.getPoolTpl()).get())
                        .withHost(this.host))
                .withPostBuilder(new SecurityPropertiesDevicePostBuilder())
                .withPostBuilder(new WebRTUNTASimultationToolPropertyPostBuilder())
                .withPostBuilder(this.setCustomAttributeValuesToDevicePostBuilderProvider.get())
                .get();
        if (this.withLocation || this.withUsagePoint || this.shouldBeActive) {
            device = deviceService.findDeviceByName(name).get();
        }
        if (this.withLocation) {
            AddLocationInfoToDevicesCommand addLocationInfoToDevicesCommand = this.addLocationInfoToDevicesCommandProvider.get();
            addLocationInfoToDevicesCommand.setDevices(Collections.singletonList(device));
            addLocationInfoToDevicesCommand.run();
        }
        if (this.withUsagePoint) {
            CreateUsagePointsForDevicesCommand createUsagePointsForDevicesCommand = this.createUsagePointsForDevicesCommandProvider.get();
            createUsagePointsForDevicesCommand.setDevices(Collections.singletonList(device));
            createUsagePointsForDevicesCommand.run();
        }
        if (this.shouldBeActive) {
            ActivateDevicesCommand activateDevicesCommand = activateDevicesCommandProvider.get();
            activateDevicesCommand.setDevices(Collections.singletonList(device));
            activateDevicesCommand.run();
        }
    }
}
