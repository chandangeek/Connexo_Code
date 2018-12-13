/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.commands.devices;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.commands.ActivateDevicesCommand;
import com.elster.jupiter.demo.impl.commands.AddLocationInfoToDevicesCommand;
import com.elster.jupiter.demo.impl.templates.ComScheduleTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.topology.TopologyService;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CreateHANDeviceCommand {
    private static final List<DeviceTypeTpl> SPE_DEVICE_TYPES = Arrays.asList(DeviceTypeTpl.Elster_AS1440, DeviceTypeTpl.Elster_A1800,
            DeviceTypeTpl.Landis_Gyr_ZMD, DeviceTypeTpl.Actaris_SL7000, DeviceTypeTpl.Siemens_7ED, DeviceTypeTpl.Iskra_38);

    private final DeviceService deviceService;
    private final TopologyService topologyService;
    private final SecurityManagementService securityManagementService;
    private final Provider<ActivateDevicesCommand> activateDevicesCommandProvider;
    private final Clock clock;

    private DeviceTypeTpl deviceTypeTpl;
    private DeviceConfiguration deviceConfiguration;
    private String serialNumber;
    private String deviceName;
    private String linkToDeviceName;
    private ComScheduleTpl comschedule;
    private ImmutableMap<String, String> securityAccesors;
    private KeyAccessorValuePersister keyAccessorValuePersister;

    @Inject
    public CreateHANDeviceCommand(DeviceService deviceService,
                                  TopologyService topologyService,
                                  SecurityManagementService securityManagementService,
                                  Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider,
                                  Provider<ActivateDevicesCommand> activateDevicesCommandProvider,
                                  Clock clock) {
        this.deviceService = deviceService;
        this.topologyService = topologyService;
        this.securityManagementService = securityManagementService;
        this.activateDevicesCommandProvider = activateDevicesCommandProvider;
        this.clock = clock;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setDeviceTypeTpl(DeviceTypeTpl deviceTypeTpl) {
        this.deviceTypeTpl = deviceTypeTpl;
    }

    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        if (this.deviceTypeTpl == null) {
            throw new UnableToCreate("Please specify the device type first");
        }
        this.deviceConfiguration = deviceConfiguration;
    }

    public void linkTo(String deviceName) {
        this.linkToDeviceName = deviceName;
    }

    public void withComSchedule(ComScheduleTpl comSchedule) {
        this.comschedule = comSchedule;
    }

    public void withSecurityAccesors(ImmutableMap<String, String> securityAccesors) {
        this.securityAccesors = securityAccesors;
    }

    public String run() {
        if (this.serialNumber == null) {
            throw new UnableToCreate("Please specify the serial number for device");
        }
        if (this.deviceTypeTpl == null) {
            throw new UnableToCreate("No devicetype given");
        }
        if (this.deviceConfiguration == null) {
            throw new UnableToCreate("This device type has no device configurations");
        }
        if(this.linkToDeviceName == null) {
            throw new UnableToCreate("Can't create gas device without master");
        }
        Device master = deviceService.findDeviceByName(linkToDeviceName).get();
        if(master.getState().getName().equals(DefaultState.ACTIVE.getKey())) {
            DeviceBuilder deviceBuilder = Builders.from(DeviceBuilder.class)
                    .withName(deviceName)
                    .withShippingDate(this.clock.instant().minus(30, ChronoUnit.DAYS))
                    .withSerialNumber(this.serialNumber)
                    .withDeviceConfiguration(this.deviceConfiguration)
                    .withComSchedules(Collections.singletonList(Builders.from(comschedule).get()));
            deviceBuilder.get();

            Device device = deviceService.findDeviceByName(deviceName).get();
            master.getLocation().ifPresent(device::setLocation);
            master.getSpatialCoordinates().ifPresent(device::setSpatialCoordinates);
            addSecurityPropertiesToDevice(device);
            device.save();
            topologyService.setPhysicalGateway(device, master);
            ActivateDevicesCommand activateDevicesCommand = activateDevicesCommandProvider.get();
            activateDevicesCommand.setDevices(Collections.singletonList(device));
            activateDevicesCommand.run();

            return deviceName;
        }
        return "";
    }

    private void addSecurityPropertiesToDevice(Device device) {
        if (securityAccesors != null) {
            securityAccesors.forEach((name, value) ->
                    getKeyAccessorValuePersister().persistKeyAccessorValue(device, name, value)
            );
        }
    }

    private KeyAccessorValuePersister getKeyAccessorValuePersister() {
        if (keyAccessorValuePersister == null) {
            keyAccessorValuePersister = new KeyAccessorValuePersister(securityManagementService);
        }
        return keyAccessorValuePersister;
    }
}
