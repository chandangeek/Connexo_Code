package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
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
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
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
import java.util.*;
import java.util.stream.Collectors;

public class LoadProfileConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;
    private final MdcReadingTypeUtilService mdcReadingTypeUtilService;

    @Inject
    public LoadProfileConfigurationResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, MasterDataService masterDataService, Thesaurus thesaurus, MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    @GET
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
            loadProfileSpecInfos.add(LoadProfileSpecInfo.from(spec, spec.getChannelSpecs(), mdcReadingTypeUtilService));
        }
        return Response.ok(PagedInfoList.fromPagedList("data", loadProfileSpecInfos, queryParameters)).build();
    }

    @GET
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
        return Response.ok(PagedInfoList.fromPagedList("data", LoadProfileTypeInfo.from(loadProfileTypes), queryParameters)).build();
    }

    @GET
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getLoadProfileSpec(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = resourceHelper.findLoadProfileSpecOrThrowException(loadProfileSpecId);
        return Response.ok(LoadProfileSpecInfo.from(loadProfileSpec, loadProfileSpec.getChannelSpecs(), mdcReadingTypeUtilService)).build();
    }

    @POST
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
        return Response.ok(LoadProfileSpecInfo.from(newLoadProfileSpec, null, mdcReadingTypeUtilService)).build();
    }

    @PUT
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response editLoadProfileSpecOnDeviceConfiguration(@PathParam("loadProfileSpecId") long loadProfileSpecId, LoadProfileSpecInfo info) {
        info.id = loadProfileSpecId;
        LoadProfileSpec loadProfileSpec = resourceHelper.lockLoadProfileSpecOrThrowException(info);
        LoadProfileSpec.LoadProfileSpecUpdater specUpdater = loadProfileSpec.getDeviceConfiguration().getLoadProfileSpecUpdaterFor(loadProfileSpec);
        specUpdater.setOverruledObisCode(info.overruledObisCode).update();
        return Response.ok(LoadProfileSpecInfo.from(loadProfileSpec, null, mdcReadingTypeUtilService)).build();
    }

    @DELETE
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

    @GET
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getAllChannelsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = resourceHelper.findLoadProfileSpecOrThrowException(loadProfileSpecId);
        List<ChannelSpec> channelSpecs = loadProfileSpec.getChannelSpecs().stream().sorted(new LoadProfileChannelComparator()).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("data", ChannelSpecFullInfo.from(channelSpecs), queryParameters)).build();
    }

    @GET
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

        return Response.ok(ChannelSpecFullInfo.from(
                channelSpec,
                channelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getCollectedReadingTypeFromChannelSpec(channelSpec)),
                deviceConfiguration.isActive())).build();
    }

    private ReadingType getCollectedReadingTypeFromChannelSpec(ChannelSpec channelSpec) {
        return channelSpec.getReadingType().getCalculatedReadingType().orElse(channelSpec.getReadingType());
    }

    @POST
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
            channelBuilder.useMultiplier(true);
        } else {
            channelBuilder.useMultiplier(false);
        }
        channelBuilder.calculatedReadingType(findCalculatedReadingType(info).orElse(null));
        ChannelSpec newChannelSpec = channelBuilder.add();
        return Response.ok(ChannelSpecFullInfo.from(
                newChannelSpec,
                newChannelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getCollectedReadingTypeFromChannelSpec(newChannelSpec)),
                deviceConfiguration.isActive())).build();
    }

    private Optional<ReadingType> findCalculatedReadingType(ChannelSpecFullInfo info) {
        if(info.calculatedReadingType != null){
            return resourceHelper.findReadingType(info.calculatedReadingType.mRID);
        } else {
            return Optional.empty();
        }
    }

    @PUT
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

        if (info.measurementType != null && info.measurementType.id > 0) {
            channelSpec.setChannelType(resourceHelper.findChannelTypeByIdOrThrowException(info.measurementType.id));
        }
        ChannelSpec.ChannelSpecUpdater specUpdater = deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        specUpdater.overruledObisCode(info.overruledObisCode);
        specUpdater.overflow(info.overflowValue);
        specUpdater.nbrOfFractionDigits(info.nbrOfFractionDigits);
        if(info.useMultiplier != null && info.useMultiplier){
            specUpdater.useMultiplier(true);
        } else {
            specUpdater.useMultiplier(false);
        }
        specUpdater.calculatedReadingType(findCalculatedReadingType(info).orElse(null));
        specUpdater.update();
        return Response.ok(ChannelSpecFullInfo.from(
                channelSpec,
                channelSpec.getReadingType(),
                getPossibleMultiplyReadingTypesFor(getCollectedReadingTypeFromChannelSpec(channelSpec)),
                deviceConfiguration.isActive())).build();
    }

    @DELETE
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

    @GET
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
                .map(channelType -> {
                    ReadingType readingTypeForMultiplyCalculation = channelType.getReadingType().getCalculatedReadingType().orElse(channelType.getReadingType());
                    return new ChannelSpecShortInfo(
                            channelType,
                            channelType.getReadingType(),
                            getPossibleMultiplyReadingTypesFor(readingTypeForMultiplyCalculation)
                    );
                }).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("data", channelSpecShortInfos, queryParameters)).build();
    }

    private List<ReadingType> getPossibleMultiplyReadingTypesFor(ReadingType readingType) {
        return masterDataService.getOrCreatePossibleMultiplyReadingTypesFor(readingType);
    }

    public Collection<LoadProfileType> findAvailableLoadProfileTypesForDeviceConfiguration(DeviceType deviceType, DeviceConfiguration deviceConfiguration){
        // Put all load profile types which exist on device type
        List<LoadProfileType> deviceTypeLoadProfileTypes = deviceType.getLoadProfileTypes();
        Map<Long, LoadProfileType> availableLoadProfileTypesForDeviceConfiguartion = new HashMap<>(deviceTypeLoadProfileTypes.size());
        for (LoadProfileType loadProfileType : deviceTypeLoadProfileTypes) {
            availableLoadProfileTypesForDeviceConfiguartion.put(loadProfileType.getId(), loadProfileType);
        }
        // Remove already assigned
        List<LoadProfileSpec> deviceConfigurationLoadProfileSpecs = deviceConfiguration.getLoadProfileSpecs();
        for (LoadProfileSpec loadProfileSpec : deviceConfigurationLoadProfileSpecs) {
            availableLoadProfileTypesForDeviceConfiguartion.remove(loadProfileSpec.getLoadProfileType().getId());
        }
        return availableLoadProfileTypesForDeviceConfiguartion.values();
    }
}