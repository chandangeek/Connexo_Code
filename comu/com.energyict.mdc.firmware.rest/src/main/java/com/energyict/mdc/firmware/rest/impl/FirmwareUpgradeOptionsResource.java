package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Path("/devicetypes/{deviceTypeId}/firmwareupgradeoptions/{id}")
public class FirmwareUpgradeOptionsResource {
    private final ResourceHelper resourceHelper;
    private final FirmwareService firmwareService;
    private final Thesaurus thesaurus;

    @Inject
    public FirmwareUpgradeOptionsResource(ResourceHelper resourceHelper, FirmwareService firmwareService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareUpgradeOptionsInfo getFirmwareUpgradeOptions(@PathParam("deviceTypeId") long deviceTypeId) {
        DeviceType deviceType =  resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

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
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

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
            throw new FirmwareUpgradeOptionsRequiredException(thesaurus);
        }
        FirmwareUpgradeOptions options = firmwareService.findOrCreateFirmwareUpgradeOptions(deviceType);
        options.setOptions(allowedFirmwareUpgradeOptions);
        firmwareService.saveFirmwareUpgradeOptions(options);

        supportedFirmwareUpgradeOptions.stream().forEach(op -> firmwareUpgradeOptionsInfo.supportedOptions.add(new UpgradeOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        allowedFirmwareUpgradeOptions.stream().forEach(op -> firmwareUpgradeOptionsInfo.allowedOptions.add(new UpgradeOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));

        return firmwareUpgradeOptionsInfo;
    }
}
