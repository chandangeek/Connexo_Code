/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.demo.impl.builders.DeviceBuilder;
import com.elster.jupiter.demo.impl.builders.device.ConnectionsDevicePostBuilder;
import com.elster.jupiter.demo.impl.builders.device.SetCustomAttributeValuesToDevicePostBuilder;
import com.elster.jupiter.demo.impl.commands.devices.KeyAccessorValuePersister;
import com.elster.jupiter.pki.SecurityManagementService;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;

public class CreateDeviceCommand extends CommandWithTransaction {

    private final DeviceService deviceService;
    private final Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider;
    private final Provider<ActivateDevicesCommand> activateDevicesCommandProvider;
    private final Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider;
    private final Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider;
    private final Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider;
    private final Clock clock;
    private KeyAccessorValuePersister keyAccessorValuePersister;
    private final SecurityManagementService securityManagementService;

    private DeviceConfiguration deviceConfiguration;
    private String serialNumber;
    private String authenticationKey = "00000000000000000000000000000001";
    private String encryptionKey = "00000000000000000000000000000001";
    private Instant activationDate;

    @Inject
    public CreateDeviceCommand(DeviceService deviceService, SecurityManagementService securityManagementService,
                               Provider<ConnectionsDevicePostBuilder> connectionsDevicePostBuilderProvider,
                               Provider<ActivateDevicesCommand> activateDevicesCommandProvider,
                               Provider<SetCustomAttributeValuesToDevicePostBuilder> setCustomAttributeValuesToDevicePostBuilderProvider,
                               Provider<AddLocationInfoToDevicesCommand> addLocationInfoToDevicesCommandProvider,
                               Provider<CreateUsagePointsForDevicesCommand> createUsagePointsForDevicesCommandProvider, Clock clock) {
        this.deviceService = deviceService;
        this.securityManagementService = securityManagementService;
        this.connectionsDevicePostBuilderProvider = connectionsDevicePostBuilderProvider;
        this.activateDevicesCommandProvider = activateDevicesCommandProvider;
        this.setCustomAttributeValuesToDevicePostBuilderProvider = setCustomAttributeValuesToDevicePostBuilderProvider;
        this.addLocationInfoToDevicesCommandProvider = addLocationInfoToDevicesCommandProvider;
        this.createUsagePointsForDevicesCommandProvider = createUsagePointsForDevicesCommandProvider;
        this.clock = clock;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        this.deviceConfiguration = deviceConfiguration;
    }

    public void withAuthenticationKey(String authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public void withEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public void withActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    public void run() {
        if (this.serialNumber == null) {
            throw new UnableToCreate("Please specify the serial number for device");
        }
        if (this.deviceConfiguration == null) {
            throw new UnableToCreate("Please specify the device configuration");
        }
        if (this.activationDate == null) {
            throw new UnableToCreate("Please specify the activation date");
        }
        String name = this.serialNumber;

        DeviceBuilder deviceBuilder = Builders.from(DeviceBuilder.class)
                .withName(name)
                .withShippingDate(this.activationDate.minusSeconds(60))
                .withSerialNumber(this.serialNumber)
                .withDeviceConfiguration(this.deviceConfiguration);

        Device device = deviceBuilder.get();

        addSecurityPropertiesToDevice(device, "AuthenticationKey", this.authenticationKey);
        addSecurityPropertiesToDevice(device, "EncryptionKey", this.encryptionKey);

        device.save();

        ActivateDevicesCommand activateDevicesCommand = activateDevicesCommandProvider.get();
        activateDevicesCommand.setDevices(Collections.singletonList(device));
        activateDevicesCommand.setTransitionDate(this.activationDate);
        activateDevicesCommand.run();
    }

    private void addSecurityPropertiesToDevice(Device device, String keyAccessorTypeName, String content) {
        getKeyAccessorValuePersister().persistKeyAccessorValue(device, keyAccessorTypeName, content);
    }

    private KeyAccessorValuePersister getKeyAccessorValuePersister() {
        if (keyAccessorValuePersister == null) {
            keyAccessorValuePersister = new KeyAccessorValuePersister(securityManagementService);
        }
        return keyAccessorValuePersister;
    }
}
