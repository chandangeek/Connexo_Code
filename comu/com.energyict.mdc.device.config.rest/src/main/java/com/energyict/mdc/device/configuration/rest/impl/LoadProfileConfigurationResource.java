/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfoFactory;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LoadProfileConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;
    private MdcReadingTypeUtilService readingTypeUtilService;
    private final ChannelSpecInfoFactory channelSpecInfoFactory;
    private final LoadProfileSpecInfoFactory loadProfileSpecInfoFactory;
    private final LoadProfileTypeInfoFactory loadProfileTypeInfoFactory;

    @Inject
    public LoadProfileConfigurationResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, MasterDataService masterDataService, Thesaurus thesaurus, MdcReadingTypeUtilService readingTypeUtilService, ChannelSpecInfoFactory channelSpecInfoFactory, LoadProfileSpecInfoFactory loadProfileSpecInfoFactory, LoadProfileTypeInfoFactory loadProfileTypeInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
        this.readingTypeUtilService = readingTypeUtilService;
        this.channelSpecInfoFactory = channelSpecInfoFactory;
        this.loadProfileSpecInfoFactory = loadProfileSpecInfoFactory;
        this.loadProfileTypeInfoFactory = loadProfileTypeInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getLoadProfileSpecsForDeviceConfiguration(
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(deviceConfiguration.getLoadProfileSpecs());
        Collections.sort(loadProfileSpecs, new LoadProfileSpecComparator());
        List<LoadProfileSpecInfo> loadProfileSpecInfos = new ArrayList<>(loadProfileSpecs.size());
        for (LoadProfileSpec spec : loadProfileSpecs) {
            loadProfileSpecInfos.add(loadProfileSpecInfoFactory.from(spec, spec.getChannelSpecs()));
        }
        return Response.ok(PagedInfoList.fromPagedList("data", loadProfileSpecInfos, queryParameters)).build();
    }

    @GET @Transactional
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getAvailableLoadProfileSpecsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        Collection<LoadProfileType> loadProfileTypes = findAvailableLoadProfileTypesForDeviceConfiguration(deviceType, deviceConfiguration);
        return Response.ok(PagedInfoList.fromPagedList("data", loadProfileTypeInfoFactory.from(loadProfileTypes), queryParameters)).build();
    }

    @GET @Transactional
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getLoadProfileSpec(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = resourceHelper.findLoadProfileSpecOrThrowException(loadProfileSpecId);
        return Response.ok(loadProfileSpecInfoFactory.from(loadProfileSpec, loadProfileSpec.getChannelSpecs())).build();
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createLoadProfileSpecForDeviceConfiguartion(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            LoadProfileSpecInfo request) {
        if (request.id == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING);
        }
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        LoadProfileType loadProfileType = resourceHelper.findLoadProfileTypeByIdOrThrowException(request.id);

        LoadProfileSpec.LoadProfileSpecBuilder specBuilder = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        if (request.overruledObisCode != null){
            specBuilder.setOverruledObisCode(request.overruledObisCode);
        }
        LoadProfileSpec newLoadProfileSpec = specBuilder.add();
        return Response.ok(loadProfileSpecInfoFactory.from(newLoadProfileSpec, null)).build();
    }

    @PUT @Transactional
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response editLoadProfileSpecOnDeviceConfiguration(@PathParam("loadProfileSpecId") long loadProfileSpecId, LoadProfileSpecInfo info) {
        info.id = loadProfileSpecId;
        LoadProfileSpec loadProfileSpec = resourceHelper.lockLoadProfileSpecOrThrowException(info);
        LoadProfileSpec.LoadProfileSpecUpdater specUpdater = loadProfileSpec.getDeviceConfiguration().getLoadProfileSpecUpdaterFor(loadProfileSpec);
        specUpdater.setOverruledObisCode(info.overruledObisCode).update();
        return Response.ok(loadProfileSpecInfoFactory.from(loadProfileSpec, null)).build();
    }

    @DELETE @Transactional
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteLoadProfileSpecFromDeviceConfiguration(
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters, LoadProfileSpecInfo info) {
        info.id = loadProfileSpecId;
        LoadProfileSpec loadProfileSpec = resourceHelper.lockLoadProfileSpecOrThrowException(info);
        loadProfileSpec.getDeviceConfiguration().deleteLoadProfileSpec(loadProfileSpec);
        return Response.ok().build();
    }

    @GET @Transactional
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getAllChannelsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = resourceHelper.findLoadProfileSpecOrThrowException(loadProfileSpecId);
        List<ChannelSpec> channelSpecs = loadProfileSpec.getChannelSpecs().stream().sorted(new LoadProfileChannelComparator()).collect(Collectors.toList());
        List<ChannelSpecInfo> channelSpecInfos = channelSpecs.stream()
                .map(channelSpec -> channelSpecInfoFactory.asFullInfo(
                channelSpec,
                channelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getReadingTypeForMultiplyCalculation(channelSpec.getReadingType())),
                loadProfileSpec.getDeviceConfiguration().isActive())).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("data", channelSpecInfos, queryParameters)).build();
    }

    @GET @Transactional
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getChannelForDeviceConfiguration(
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @PathParam("channelId") long channelId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationByIdOrThrowException(deviceConfigurationId);
        ChannelSpec channelSpec = resourceHelper.findChannelSpecOrThrowException(channelId);

        return Response.ok(channelSpecInfoFactory.asFullInfo(
                channelSpec,
                channelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getReadingTypeForMultiplyCalculation(channelSpec.getReadingType())),
                deviceConfiguration.isActive())).build();
    }

    @POST @Transactional
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response createChannelForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            ChannelSpecFullInfo info) {
        LoadProfileSpec loadProfileSpec = resourceHelper.findLoadProfileSpecOrThrowException(loadProfileSpecId);
        ChannelType channelType = resourceHelper.findChannelTypeByIdOrThrowException(info.measurementType.id);
        DeviceConfiguration deviceConfiguration = loadProfileSpec.getDeviceConfiguration();

        ChannelSpec.ChannelSpecBuilder channelBuilder = deviceConfiguration.createChannelSpec(channelType, loadProfileSpec);
        channelBuilder.overflow(info.overflowValue);
        channelBuilder.overruledObisCode(info.overruledObisCode);
        channelBuilder.nbrOfFractionDigits(info.nbrOfFractionDigits);
        if(info.useMultiplier != null && info.useMultiplier){
            channelBuilder.useMultiplierWithCalculatedReadingType(findCalculatedReadingType(info).orElse(null));
        } else {
            channelBuilder.noMultiplier();
        }
        ChannelSpec newChannelSpec = channelBuilder.add();
        return Response.ok(channelSpecInfoFactory.asFullInfo(
                newChannelSpec,
                newChannelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getReadingTypeForMultiplyCalculation(newChannelSpec.getReadingType())),
                deviceConfiguration.isActive())).build();
    }

    private Optional<ReadingType> findCalculatedReadingType(ChannelSpecFullInfo info) {
        if(info.multipliedCalculatedReadingType != null){
            return resourceHelper.findReadingType(info.multipliedCalculatedReadingType.mRID);
        } else {
            return Optional.empty();
        }
    }

    @PUT @Transactional
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response editChannelSpecOnDeviceConfiguration(
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("channelId") long channelId,
            ChannelSpecFullInfo info) {
        info.id = channelId;
        ChannelSpec channelSpec = resourceHelper.lockChannelSpecOrThrowException(info);
        DeviceConfiguration deviceConfiguration = channelSpec.getLoadProfileSpec().getDeviceConfiguration();

        ChannelSpec.ChannelSpecUpdater specUpdater = deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        specUpdater.overruledObisCode(info.overruledObisCode);
        specUpdater.overflow(info.overflowValue);
        specUpdater.nbrOfFractionDigits(info.nbrOfFractionDigits);
        if(info.useMultiplier){
            specUpdater.useMultiplierWithCalculatedReadingType(findCalculatedReadingType(info).orElse(null));
        } else {
            specUpdater.noMultiplier();
        }
        specUpdater.update();
        return Response.ok(channelSpecInfoFactory.asFullInfo(
                channelSpec,
                channelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getReadingTypeForMultiplyCalculation(channelSpec.getReadingType())),
                deviceConfiguration.isActive())).build();
    }

    @DELETE @Transactional
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteChannelSpecFromDeviceConfiguration(
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @PathParam("channelId") long channelId,
            ChannelSpecFullInfo info) {
        ChannelSpec channelSpec = resourceHelper.lockChannelSpecOrThrowException(info);
        channelSpec.getLoadProfileSpec().getDeviceConfiguration().removeChannelSpec(channelSpec);
        return Response.ok().build();
    }

    @GET @Transactional
    @Path("{loadProfileSpecId}/measurementTypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE,Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getAvailableMeasurementTypesForChannel(
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = resourceHelper.findLoadProfileSpecOrThrowException(loadProfileSpecId);
        Map<Long, ChannelType> channelTypes = new HashMap<>();
        for (ChannelType measurementType : loadProfileSpec.getLoadProfileType().getChannelTypes()) {
            channelTypes.put(measurementType.getId(), measurementType);
        }
        List<ChannelSpec> channelSpecs = loadProfileSpec.getChannelSpecs();
        for (ChannelSpec channelSpec : channelSpecs) {
            channelTypes.remove(channelSpec.getChannelType().getId());
        }
        List<ChannelSpecShortInfo> channelSpecShortInfos = channelTypes.values().stream()
                .map(channelType -> channelSpecInfoFactory.asShortInfo(
                        channelType,
                        channelType.getReadingType(),
                        getPossibleMultiplyReadingTypesFor(getReadingTypeForMultiplyCalculation(channelType.getReadingType()))
                )).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("data", channelSpecShortInfos, queryParameters)).build();
    }

    private ReadingType getReadingTypeForMultiplyCalculation(ReadingType readingType) {
        if(readingType.isCumulative()){
            Optional<ReadingType> calculatedReadingType = readingType.getCalculatedReadingType();
            if(calculatedReadingType.isPresent()){
                return calculatedReadingType.get();
            } else {
                return readingTypeUtilService.findOrCreateReadingType(readingTypeUtilService.createReadingTypeCodeBuilderFrom(readingType).accumulate(Accumulation.DELTADELTA).code(), readingType.getAliasName());
            }
        } else {
            return readingType;
        }
    }

    private List<ReadingType> getPossibleMultiplyReadingTypesFor(ReadingType readingType) {
        return masterDataService.getOrCreatePossibleMultiplyReadingTypesFor(readingType);
    }

    public Collection<LoadProfileType> findAvailableLoadProfileTypesForDeviceConfiguration(DeviceType deviceType, DeviceConfiguration deviceConfiguration){
        // Put all load profile types which exist on device type
        List<LoadProfileType> deviceTypeLoadProfileTypes = deviceType.getLoadProfileTypes();
        Map<Long, LoadProfileType> availableLoadProfileTypesForDeviceConfiguration = new HashMap<>(deviceTypeLoadProfileTypes.size());
        for (LoadProfileType loadProfileType : deviceTypeLoadProfileTypes) {
            availableLoadProfileTypesForDeviceConfiguration.put(loadProfileType.getId(), loadProfileType);
        }
        // Remove already assigned
        List<LoadProfileSpec> deviceConfigurationLoadProfileSpecs = deviceConfiguration.getLoadProfileSpecs();
        for (LoadProfileSpec loadProfileSpec : deviceConfigurationLoadProfileSpecs) {
            availableLoadProfileTypesForDeviceConfiguration.remove(loadProfileSpec.getLoadProfileType().getId());
        }
        return availableLoadProfileTypesForDeviceConfiguration.values();
    }
}