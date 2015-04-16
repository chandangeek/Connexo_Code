package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Path("/devicetypes/{deviceTypeId}/firmwareupgradeoptions/{id}")
public class FirmwareUpgradeOptionsResource {
    private final DeviceConfigurationService deviceConfigurationService;
    private final FirmwareService firmwareService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public FirmwareUpgradeOptionsResource(DeviceConfigurationService deviceConfigurationService, FirmwareService firmwareService, Thesaurus thesaurus, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareUpgradeOptionsInfo getFirmwareUpgradeOptions(@PathParam("deviceTypeId") long deviceTypeId) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareUpgradeOptionsInfo firmwareUpgradeOptionsInfo = new FirmwareUpgradeOptionsInfo();
        Set<ProtocolSupportedFirmwareOptions> supportedFirmwareUpgradeOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);
        Set<ProtocolSupportedFirmwareOptions> allowedFirmwareUpgradeOptions = firmwareService.getAllowedFirmwareUpgradeOptionsFor(deviceType);

        supportedFirmwareUpgradeOptions.stream().forEach(op -> firmwareUpgradeOptionsInfo.supportedOptions.add(new UpgradeOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));

        allowedFirmwareUpgradeOptions.stream().forEach(op ->
                firmwareUpgradeOptionsInfo.allowedOptions.add(new UpgradeOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        if (!allowedFirmwareUpgradeOptions.isEmpty()) {
            firmwareUpgradeOptionsInfo.isAllowed = true;
        }
        return firmwareUpgradeOptionsInfo;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareUpgradeOptionsInfo editFirmwareUpgradeOptions(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareUpgradeOptionsInfo inputOptions) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareUpgradeOptionsInfo firmwareUpgradeOptionsInfo = new FirmwareUpgradeOptionsInfo();
        Set<ProtocolSupportedFirmwareOptions> supportedFirmwareUpgradeOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        Set<ProtocolSupportedFirmwareOptions> allowedFirmwareUpgradeOptions = new LinkedHashSet<>();
        inputOptions.allowedOptions.stream().forEach(op ->
            {
                Optional<ProtocolSupportedFirmwareOptions> optionRef = ProtocolSupportedFirmwareOptions.from(op.id);
                if(optionRef.isPresent()) {
                    allowedFirmwareUpgradeOptions.add(optionRef.get());
                }
            });
        if (allowedFirmwareUpgradeOptions.isEmpty() && inputOptions.isAllowed) {
            throw exceptionFactory.newException(MessageSeeds.UPGRADE_OPTIONS_REQUIRED);
        }
        FirmwareUpgradeOptions options = firmwareService.getFirmwareUpgradeOptions(deviceType);
        options.setOptions(allowedFirmwareUpgradeOptions);
        firmwareService.saveFirmwareUpgradeOptions(options);

        supportedFirmwareUpgradeOptions.stream().forEach(op -> firmwareUpgradeOptionsInfo.supportedOptions.add(new UpgradeOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        allowedFirmwareUpgradeOptions.stream().forEach(op -> firmwareUpgradeOptionsInfo.allowedOptions.add(new UpgradeOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));

        return firmwareUpgradeOptionsInfo;
    }


    private DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }
}
