/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.device.configuration.rest.RegisterConfigInfo;
import com.energyict.mdc.device.configuration.rest.RegisterConfigurationComparator;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegisterConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final MasterDataService masterDataService;
    private final RegisterConfigInfoFactory registerConfigInfoFactory;

    @Inject
    public RegisterConfigurationResource(ResourceHelper resourceHelper, MasterDataService masterDataService, RegisterConfigInfoFactory registerConfigInfoFactory) {
        super();
        this.resourceHelper = resourceHelper;
        this.masterDataService = masterDataService;
        this.registerConfigInfoFactory = registerConfigInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public PagedInfoList getRegisterConfigs(@PathParam("deviceConfigurationId") long deviceConfigurationId, @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<RegisterSpec> pagedRegisterSpecs = ListPager.of(deviceConfiguration.getRegisterSpecs(), new RegisterConfigurationComparator()).from(queryParameters).find();
        List<RegisterConfigInfo> registerConfigInfos = pagedRegisterSpecs.stream()
                .map(registerSpec -> registerConfigInfoFactory.from(registerSpec, getPossibleMultiplyReadingTypesFor(registerSpec
                        .getReadingType())))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("registerConfigurations", registerConfigInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{registerId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public RegisterConfigInfo getRegisterConfigs(@PathParam("registerId") long registerId) {
        RegisterSpec registerSpec = resourceHelper.findRegisterSpecByIdOrThrowException(registerId);
        return registerConfigInfoFactory.from(registerSpec, getPossibleMultiplyReadingTypesFor(registerSpec.getReadingType()));
    }

    private List<ReadingType> getPossibleMultiplyReadingTypesFor(ReadingType readingType) {
        return masterDataService.getOrCreatePossibleMultiplyReadingTypesFor(readingType);
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createRegisterConfig(@PathParam("deviceConfigurationId") long deviceConfigurationId, RegisterConfigInfo registerConfigInfo) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        RegisterType registerType = registerConfigInfo.registerType ==null?null: findRegisterTypeOrThrowException(registerConfigInfo.registerType);
        RegisterSpec registerSpec = createRegisterSpec(registerConfigInfo, deviceConfiguration, registerType);
        return Response.status(Response.Status.CREATED)
                .entity(registerConfigInfoFactory.from(registerSpec, getPossibleMultiplyReadingTypesFor(registerSpec.getReadingType())))
                .build();
    }

    private RegisterSpec createRegisterSpec(RegisterConfigInfo registerConfigInfo, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
        RegisterSpec registerSpec;
        if(registerConfigInfo.asText){
            registerSpec = deviceConfiguration.createTextualRegisterSpec(registerType)
                    .setOverruledObisCode(registerConfigInfo.overruledObisCode)
                    .add();
        } else {
            NumericalRegisterSpec.Builder builder = deviceConfiguration.createNumericalRegisterSpec(registerType)
                    .numberOfFractionDigits(registerConfigInfo.numberOfFractionDigits)
                    .overflowValue(registerConfigInfo.overflow)
                    .overruledObisCode(registerConfigInfo.overruledObisCode);
            if (registerConfigInfo.useMultiplier != null && registerConfigInfo.useMultiplier) {
                builder.useMultiplierWithCalculatedReadingType(findCalculatedReadingType(registerConfigInfo).orElse(null));
            } else {
                builder.noMultiplier();
            }
            registerSpec = builder.add();
        }
        return registerSpec;
    }

    @PUT @Transactional
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public RegisterConfigInfo updateRegisterConfig(@PathParam("registerConfigId") long registerConfigId, RegisterConfigInfo info) {
        info.id = registerConfigId;
        RegisterSpec registerSpec = resourceHelper.lockRegisterSpecOrThrowException(info);
        RegisterType registerType = info.registerType == null ? null : resourceHelper.findRegisterTypeByIdOrThrowException(info.registerType);
        Optional<ReadingType> calculatedReadingType = findCalculatedReadingType(info);
        if (stillTheSameDiscriminator(info, registerSpec)) {
            if (info.asText) {
                TextualRegisterSpec.Updater registerSpecUpdater = registerSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor(((TextualRegisterSpec) registerSpec));
                info.writeTo(registerSpecUpdater);
                registerSpecUpdater.update();
            } else {
                NumericalRegisterSpec.Updater registerSpecUpdater = registerSpec.getDeviceConfiguration().getRegisterSpecUpdaterFor(((NumericalRegisterSpec) registerSpec));
                info.writeTo(registerSpecUpdater, calculatedReadingType.orElse(null));
                registerSpecUpdater.update();
            }
        } else {
            registerSpec.getDeviceConfiguration().deleteRegisterSpec(registerSpec);
            registerSpec = createRegisterSpec(info, registerSpec.getDeviceConfiguration(), registerType);
        }
        return registerConfigInfoFactory.from(registerSpec, getPossibleMultiplyReadingTypesFor(registerSpec.getReadingType()));
    }

    private boolean stillTheSameDiscriminator(RegisterConfigInfo info, RegisterSpec registerSpec) {
        return info.asText == registerSpec.isTextual();
    }

    private Optional<ReadingType> findCalculatedReadingType(RegisterConfigInfo info) {
        if(info.calculatedReadingType != null){
            return resourceHelper.findReadingType(info.calculatedReadingType.mRID);
        } else {
            return Optional.empty();
        }
    }

    @DELETE @Transactional
    @Path("/{registerConfigId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteRegisterConfig(@PathParam("registerConfigId") long registerConfigId, RegisterConfigInfo info) {
        info.id = registerConfigId;
        RegisterSpec registerSpec = resourceHelper.lockRegisterSpecOrThrowException(info);
        registerSpec.getDeviceConfiguration().deleteRegisterSpec(registerSpec);
        return Response.ok().build();
    }

    private RegisterType findRegisterTypeOrThrowException(Long registerTypeId) {
        Optional<RegisterType> registerType = masterDataService.findRegisterType(registerTypeId);
        if (!registerType.isPresent()) {
            throw new LocalizedFieldValidationException(MessageSeeds.INVALID_REFERENCE_TO_REGISTER_TYPE, "registerType");
        }
        return registerType.get();
    }
}
