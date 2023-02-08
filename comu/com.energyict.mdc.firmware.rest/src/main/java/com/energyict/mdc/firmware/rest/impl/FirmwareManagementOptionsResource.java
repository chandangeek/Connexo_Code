/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.device.config.DeviceConfigConstants;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/devicetypes/{deviceTypeId}/firmwaremanagementoptions/{id}")
public class FirmwareManagementOptionsResource {
    private final DeviceConfigurationService deviceConfigurationService;
    private final FirmwareService firmwareService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public FirmwareManagementOptionsResource(DeviceConfigurationService deviceConfigurationService, FirmwareService firmwareService, Thesaurus thesaurus, ExceptionFactory exceptionFactory, ConcurrentModificationExceptionFactory conflictFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.firmwareService = firmwareService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.VIEW_DEVICE_TYPE, DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareManagementOptionsInfo getFirmwareManagementOptions(@PathParam("deviceTypeId") long deviceTypeId) {
        DeviceType deviceType = findDeviceTypeOrElseThrowException(deviceTypeId);
        return getFirmwareManagementOptions(deviceType);
    }

    private FirmwareManagementOptionsInfo getFirmwareManagementOptions(DeviceType deviceType) {
        FirmwareManagementOptionsInfo firmwareManagementOptionsInfo = new FirmwareManagementOptionsInfo();
        Set<ProtocolSupportedFirmwareOptions> supportedFirmwareMgtOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);
        Optional<FirmwareManagementOptions> firmwareMgtOptions = firmwareService.findFirmwareManagementOptions(deviceType);
        Set<ProtocolSupportedFirmwareOptions> allowedMgtOptions = firmwareMgtOptions.map(FirmwareManagementOptions::getOptions).orElse(Collections.emptySet());
        supportedFirmwareMgtOptions
                .forEach(op -> firmwareManagementOptionsInfo.supportedOptions.add(new ManagementOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        allowedMgtOptions
                .forEach(op -> firmwareManagementOptionsInfo.allowedOptions.add(new ManagementOptionInfo(op.getId(), thesaurus.getString(op.getId(), op.getId()))));
        Arrays.stream(FirmwareCheckManagementOption.values()).forEach(checkManagementOption ->
                firmwareManagementOptionsInfo.checkOptions.put(checkManagementOption,
                        firmwareMgtOptions.map(options -> new CheckManagementOptionInfo(options, checkManagementOption))
                                .orElseGet(CheckManagementOptionInfo::new)));

        firmwareManagementOptionsInfo.isAllowed = !allowedMgtOptions.isEmpty();
        firmwareManagementOptionsInfo.version = firmwareMgtOptions.map(FirmwareManagementOptions::getVersion).orElse(0L);
        deviceType.getDeviceProtocolPluggableClass()
                .ifPresent(deviceProtocolPluggableClass -> firmwareManagementOptionsInfo.validateFirmwareFileSignature = deviceProtocolPluggableClass.getDeviceProtocol()
                        .firmwareSignatureCheckSupported());

        return firmwareManagementOptionsInfo;
    }

    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({DeviceConfigConstants.ADMINISTRATE_DEVICE_TYPE})
    public FirmwareManagementOptionsInfo editFirmwareManagementOptions(@PathParam("deviceTypeId") long deviceTypeId, FirmwareManagementOptionsInfo info) {
        DeviceType deviceType = findDeviceTypeOrElseThrowException(deviceTypeId);
        Optional<FirmwareManagementOptions> firmwareManagementOptions = firmwareService.findAndLockFirmwareManagementOptionsByIdAndVersion(deviceType, info.version);
        if (!firmwareManagementOptions.isPresent() && firmwareService.findFirmwareManagementOptions(deviceType).isPresent()) {
            throw conflictFactory.contextDependentConflictOn(deviceType.getName())
                    .withActualVersion(() -> firmwareService.findFirmwareManagementOptions(deviceType).map(FirmwareManagementOptions::getVersion).orElse(null))
                    .build();
        }
        if (info.isAllowed && info.allowedOptions != null) {
            Set<ProtocolSupportedFirmwareOptions> supportedFirmwareOptions = firmwareService.getSupportedFirmwareOptionsFor(deviceType);
            Set<ProtocolSupportedFirmwareOptions> newAllowedOptions = info.allowedOptions.stream()
                    .map(allowedOption -> ProtocolSupportedFirmwareOptions.from(allowedOption.id))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(supportedFirmwareOptions::contains)
                    .collect(Collectors.toSet());
            FirmwareManagementOptions options = firmwareManagementOptions.orElseGet(() -> firmwareService.newFirmwareManagementOptions(deviceType));
            options.setOptions(newAllowedOptions);
            Arrays.stream(FirmwareCheckManagementOption.values()).forEach(checkManagementOption -> {
                CheckManagementOptionInfo checkInfo = info.checkOptions.get(checkManagementOption);
                if (checkInfo == null || !checkInfo.isActivated()) {
                    options.deactivate(checkManagementOption);
                } else {
                    options.activateFirmwareCheckWithStatuses(checkManagementOption, checkInfo.getStatuses());
                }
            });
            options.save();
        } else {
            firmwareManagementOptions.ifPresent(FirmwareManagementOptions::delete);
        }
        return getFirmwareManagementOptions(deviceType);
    }


    private DeviceType findDeviceTypeOrElseThrowException(long deviceTypeId) {
        return deviceConfigurationService.findDeviceType(deviceTypeId).orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_TYPE_NOT_FOUND, deviceTypeId));
    }
}
