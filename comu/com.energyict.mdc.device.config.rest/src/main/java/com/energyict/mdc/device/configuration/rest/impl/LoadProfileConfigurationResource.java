package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class LoadProfileConfigurationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;

    @Inject
    public LoadProfileConfigurationResource(ResourceHelper resourceHelper, DeviceConfigurationService deviceConfigurationService, MasterDataService masterDataService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.deviceConfigurationService = deviceConfigurationService;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoadProfileSpecsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(deviceConfiguration.getLoadProfileSpecs());
        Collections.sort(loadProfileSpecs, new LoadProfileSpecComparator());
        List<LoadProfileSpecInfo> loadProfileSpecInfos = new ArrayList<>(loadProfileSpecs.size());
        for (LoadProfileSpec spec : loadProfileSpecs) {
            loadProfileSpecInfos.add(LoadProfileSpecInfo.from(spec, deviceConfigurationService.findChannelSpecsForLoadProfileSpec(spec)));
        }
        return Response.ok(PagedInfoList.asJson("data", loadProfileSpecInfos, queryParameters)).build();
    }

    @GET
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableLoadProfileSpecsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        Collection<LoadProfileType> loadProfileTypes = findAvailableLoadProfileTypesForDeviceConfiguration(deviceType, deviceConfiguration);
        return Response.ok(PagedInfoList.asJson("data", LoadProfileTypeInfo.from(loadProfileTypes), queryParameters)).build();
    }

    @GET
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoadProfileSpec(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @BeanParam QueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        return Response.ok(PagedInfoList.asJson("data", LoadProfileSpecInfo.from(Collections.singletonList(loadProfileSpec)), queryParameters)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createLoadProfileSpecForDeviceConfiguartion(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            LoadProfileSpecInfo request) {
        if (request.id == 0) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LoadProfileType loadProfileType = findLoadProfileTypeByIdOrThrowException(request.id);

        LoadProfileSpec.LoadProfileSpecBuilder specBuilder = deviceConfiguration.createLoadProfileSpec(loadProfileType);
        if (request.overruledObisCode != null){
            specBuilder.setOverruledObisCode(request.overruledObisCode);
        }
        LoadProfileSpec newLoadProfileSpec = specBuilder.add();
        return Response.ok(LoadProfileSpecInfo.from(newLoadProfileSpec, null)).build();
    }

    @PUT
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editLoadProfileSpecOnDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @BeanParam QueryParameters queryParameters,
            LoadProfileSpecInfo request) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        LoadProfileSpec.LoadProfileSpecUpdater specUpdater = deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec);
        specUpdater.setOverruledObisCode(request.overruledObisCode).update();
        return Response.ok(LoadProfileSpecInfo.from(loadProfileSpec, null)).build();
    }

    @DELETE
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLoadProfileSpecFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        deviceConfiguration.deleteLoadProfileSpec(loadProfileSpec);
        return Response.ok().build();
    }

    @GET
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllChannelsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @BeanParam QueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        List<ChannelSpec> channelSpecs = deviceConfigurationService.findChannelSpecsForLoadProfileSpec(loadProfileSpec);
        Collections.sort(channelSpecs, new LoadProfileChannelComparator());
        return Response.ok(PagedInfoList.asJson("data", ChannelSpecFullInfo.from(channelSpecs), queryParameters)).build();
    }

    @GET
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getChannelForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @PathParam("channelId") int channelId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelSpec channelSpec = findChannelSpecByIdOrThrowException(channelId);

        return Response.ok(PagedInfoList.asJson("data", Collections.singletonList(ChannelSpecFullInfo.from(channelSpec, deviceConfiguration.isActive())), queryParameters)).build();
    }

    @POST
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createChannelForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            ChannelSpecFullInfo request) {

        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelType channelType = resourceHelper.findChannelTypeByIdOrThrowException(request.registerTypeInfo.id);
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        Phenomenon phenomenon = findPhenomenonByIdOrThrowException(request.unitOfMeasure.id);

        ChannelSpec.ChannelSpecBuilder channelBuilder = deviceConfiguration.createChannelSpec(channelType, phenomenon, loadProfileSpec);
        channelBuilder.setOverflow(request.overflowValue);
        channelBuilder.setMultiplier(request.multiplier);
        channelBuilder.setOverruledObisCode(request.overruledObisCode);

        ChannelSpec newChannelSpec = channelBuilder.add();
        return Response.ok(ChannelSpecFullInfo.from(newChannelSpec, deviceConfiguration.isActive())).build();
    }

    @PUT
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editChannelSpecOnDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @PathParam("channelId") int channelId,
            @BeanParam QueryParameters queryParameters,
            ChannelSpecFullInfo request) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelSpec channelSpec = findChannelSpecByIdOrThrowException(channelId);
        if (request.registerTypeInfo != null && request.registerTypeInfo.id > 0) {
            channelSpec.setChannelType(resourceHelper.findChannelTypeByIdOrThrowException(request.registerTypeInfo.id));
        }
        if (request.unitOfMeasure != null && request.unitOfMeasure.id > 0) {
            channelSpec.setPhenomenon(findPhenomenonByIdOrThrowException(request.unitOfMeasure.id));
        }

        ChannelSpec.ChannelSpecUpdater specUpdater = deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        specUpdater.setOverruledObisCode(request.overruledObisCode);
        specUpdater.setOverflow(request.overflowValue);
        specUpdater.setMultiplier(request.multiplier);
        specUpdater.update();
        return Response.ok(ChannelSpecFullInfo.from(channelSpec, deviceConfiguration.isActive())).build();
    }

    @DELETE
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteChannelSpecFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @PathParam("channelId") int channelId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelSpec channelSpec = findChannelSpecByIdOrThrowException(channelId);
        deviceConfiguration.deleteChannelSpec(channelSpec);
        return Response.ok().build();
    }

    @GET
    @Path("{loadProfileSpecId}/measurementTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableMeasurementTypesForChannel(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") int loadProfileSpecId,
            @BeanParam QueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        Map<Long, ChannelType> channelTypes = new HashMap<>();
        for (ChannelType measurementType : loadProfileSpec.getLoadProfileType().getChannelTypes()) {
            channelTypes.put(measurementType.getId(), measurementType);
        }
        List<ChannelSpec> channelSpecs = deviceConfigurationService.findChannelSpecsForLoadProfileSpec(loadProfileSpec);
        for (ChannelSpec channelSpec : channelSpecs) {
            channelTypes.remove(channelSpec.getChannelType().getId());
        }
        return Response.ok(PagedInfoList.asJson("data", MeasurementTypeShortInfo.from(channelTypes.values()), queryParameters)).build();
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

    private LoadProfileType findLoadProfileTypeByIdOrThrowException(long loadProfileTypeId) {
        Optional<LoadProfileType> loadProfileType = masterDataService.findLoadProfileType(loadProfileTypeId);
        if (!loadProfileType.isPresent()){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND);
        }
        return loadProfileType.get();
    }

    private LoadProfileSpec findLoadProfileSpecByIdOrThrowEception(int loadProfileSpecId) {
        LoadProfileSpec loadProfileSpec = deviceConfigurationService.findLoadProfileSpec(loadProfileSpecId);
        if (loadProfileSpec == null){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, loadProfileSpecId);
        }
        return loadProfileSpec;
    }

    private ChannelSpec findChannelSpecByIdOrThrowException(int channelId) {
        ChannelSpec channelSpec = deviceConfigurationService.findChannelSpec(channelId);
        if (channelSpec == null){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_CHANNEL_SPEC_FOUND, channelId);
        }
        return channelSpec;
    }

    private Phenomenon findPhenomenonByIdOrThrowException(int phenomenonId) {
        Phenomenon phenomenon = masterDataService.findPhenomenon(phenomenonId).orNull();
        if (phenomenon == null){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_PHENOMENON_FOUND, phenomenonId);
        }
        return phenomenon;
    }
}
