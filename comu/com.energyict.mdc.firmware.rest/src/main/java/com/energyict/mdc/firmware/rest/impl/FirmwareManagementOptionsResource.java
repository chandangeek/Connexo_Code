package com.energyict.mdc.firmware.rest.impl;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

@Path("/devicetypes/{deviceTypeId}/firmwaremanagementoptions/{id}")
public class FirmwareManagementOptionsResource {
    private final DeviceConfigurationService deviceConfigurationService;
    private final FirmwareService firmwareService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public FirmwareManagementOptionsResource(DeviceConfigurationService deviceConfigurationService, FirmwareService firmwareService, Thesaurus thesaurus, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareManagementOptionsInfo getFirmwareManagementOptions(@PathParam("deviceTypeId") long deviceTypeId) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        Set<ProtocolSupportedFirmwareOptions> supportedFirmwareMgtOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);
        Set<ProtocolSupportedFirmwareOptions> allowedFirmwareMgtOptions = firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType);

        supportedFirmwareMgtOptions.stream().forEach(op -> firmwareManagementOptionsInfo.supportedOptions.add(new ManagementOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        allowedFirmwareMgtOptions.stream().forEach(op ->
                firmwareManagementOptionsInfo.allowedOptions.add(new ManagementOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));

        firmwareManagementOptionsInfo.isAllowed = !allowedFirmwareMgtOptions.isEmpty();

        return firmwareManagementOptionsInfo;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareManagementOptionsInfo editFirmwareManagementOptions(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("id") long id, FirmwareManagementOptionsInfo inputOptions) {
        DeviceType deviceType =  findDeviceTypeOrElseThrowException(deviceTypeId);

        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        Set<ProtocolSupportedFirmwareOptions> supportedFirmwareMgtOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);

        Set<ProtocolSupportedFirmwareOptions> allowedFirmwareMgtOptions = new LinkedHashSet<>();
        inputOptions.allowedOptions.stream().forEach(op ->
            {
                Optional<ProtocolSupportedFirmwareOptions> optionRef = ProtocolSupportedFirmwareOptions.from(op.id);
                if(optionRef.isPresent()) {
                    allowedFirmwareMgtOptions.add(optionRef.get());
                }
            });
        if (allowedFirmwareMgtOptions.isEmpty() && inputOptions.isAllowed) {
            throw exceptionFactory.newException(MessageSeeds.UPGRADE_OPTIONS_REQUIRED);
        }
        FirmwareManagementOptions options = firmwareService.getFirmwareManagementOptions(deviceType);
        options.setOptions(allowedFirmwareMgtOptions);
        firmwareService.saveFirmwareManagementOptions(options);

        supportedFirmwareMgtOptions.stream().forEach(op -> firmwareManagementOptionsInfo.supportedOptions.add(new ManagementOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        allowedFirmwareMgtOptions.stream().forEach(op -> firmwareManagementOptionsInfo.allowedOptions.add(new ManagementOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));

        return firmwareManagementOptionsInfo;
    }


    private DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }
}
