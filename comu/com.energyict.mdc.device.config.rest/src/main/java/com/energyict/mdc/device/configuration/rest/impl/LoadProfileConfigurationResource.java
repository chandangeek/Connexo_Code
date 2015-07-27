package com.energyict.mdc.device.configuration.rest.impl;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getLoadProfileSpecsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
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
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getAvailableLoadProfileSpecsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        Collection<LoadProfileType> loadProfileTypes = findAvailableLoadProfileTypesForDeviceConfiguration(deviceType, deviceConfiguration);
        return Response.ok(PagedInfoList.fromPagedList("data", LoadProfileTypeInfo.from(loadProfileTypes), queryParameters)).build();
    }

    @GET
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getLoadProfileSpec(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        return Response.ok(PagedInfoList.fromPagedList("data", LoadProfileSpecInfo.from(Collections.singletonList(loadProfileSpec), mdcReadingTypeUtilService), queryParameters)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
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
        return Response.ok(LoadProfileSpecInfo.from(newLoadProfileSpec, null, mdcReadingTypeUtilService)).build();
    }

    @PUT
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response editLoadProfileSpecOnDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters,
            LoadProfileSpecInfo request) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        LoadProfileSpec.LoadProfileSpecUpdater specUpdater = deviceConfiguration.getLoadProfileSpecUpdaterFor(loadProfileSpec);
        specUpdater.setOverruledObisCode(request.overruledObisCode).update();
        return Response.ok(LoadProfileSpecInfo.from(loadProfileSpec, null, mdcReadingTypeUtilService)).build();
    }

    @DELETE
    @Path("/{loadProfileSpecId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteLoadProfileSpecFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        deviceConfiguration.deleteLoadProfileSpec(loadProfileSpec);
        return Response.ok().build();
    }

    @GET
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getAllChannelsForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        List<ChannelSpec> channelSpecs = loadProfileSpec.getChannelSpecs();
        Collections.sort(channelSpecs, new LoadProfileChannelComparator());
        return Response.ok(PagedInfoList.fromPagedList("data", ChannelSpecFullInfo.from(channelSpecs), queryParameters)).build();
    }

    @GET
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getChannelForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @PathParam("channelId") long channelId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelSpec channelSpec = findChannelSpecByIdOrThrowException(channelId);

        return Response.ok(PagedInfoList.fromPagedList("data", Collections.singletonList(ChannelSpecFullInfo.from(channelSpec, deviceConfiguration.isActive())), queryParameters)).build();
    }

    @POST
    @Path("{loadProfileSpecId}/channels")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response createChannelForDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            ChannelSpecFullInfo request) {

        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelType channelType = resourceHelper.findChannelTypeByIdOrThrowException(request.registerTypeInfo.id);
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);

        ChannelSpec.ChannelSpecBuilder channelBuilder = deviceConfiguration.createChannelSpec(channelType, loadProfileSpec);
        channelBuilder.setOverflow(request.overflowValue);
        channelBuilder.setOverruledObisCode(request.overruledObisCode);
        channelBuilder.setNbrOfFractionDigits(request.nbrOfFractionDigits);

        ChannelSpec newChannelSpec = channelBuilder.add();
        return Response.ok(ChannelSpecFullInfo.from(newChannelSpec, deviceConfiguration.isActive())).build();
    }

    @PUT
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response editChannelSpecOnDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @PathParam("channelId") long channelId,
            @BeanParam JsonQueryParameters queryParameters,
            ChannelSpecFullInfo request) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelSpec channelSpec = findChannelSpecByIdOrThrowException(channelId);
        if (request.registerTypeInfo != null && request.registerTypeInfo.id > 0) {
            channelSpec.setChannelType(resourceHelper.findChannelTypeByIdOrThrowException(request.registerTypeInfo.id));
        }
        ChannelSpec.ChannelSpecUpdater specUpdater = deviceConfiguration.getChannelSpecUpdaterFor(channelSpec);
        specUpdater.setOverruledObisCode(request.overruledObisCode);
        specUpdater.setOverflow(request.overflowValue);
        specUpdater.setNbrOfFractionDigits(request.nbrOfFractionDigits);
        specUpdater.update();
        return Response.ok(ChannelSpecFullInfo.from(channelSpec, deviceConfiguration.isActive())).build();
    }

    @DELETE
    @Path("{loadProfileSpecId}/channels/{channelId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteChannelSpecFromDeviceConfiguration(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @PathParam("channelId") long channelId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        DeviceConfiguration deviceConfiguration = resourceHelper.findDeviceConfigurationForDeviceTypeOrThrowException(deviceType, deviceConfigurationId);
        ChannelSpec channelSpec = findChannelSpecByIdOrThrowException(channelId);
        deviceConfiguration.removeChannelSpec(channelSpec);
        return Response.ok().build();
    }

    @GET
    @Path("{loadProfileSpecId}/measurementTypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE,Privileges.VIEW_DEVICE_TYPE})
    public Response getAvailableMeasurementTypesForChannel(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigurationId") long deviceConfigurationId,
            @PathParam("loadProfileSpecId") long loadProfileSpecId,
            @BeanParam JsonQueryParameters queryParameters) {
        LoadProfileSpec loadProfileSpec = findLoadProfileSpecByIdOrThrowEception(loadProfileSpecId);
        Map<Long, ChannelType> channelTypes = new HashMap<>();
        for (ChannelType measurementType : loadProfileSpec.getLoadProfileType().getChannelTypes()) {
            channelTypes.put(measurementType.getId(), measurementType);
        }
        List<ChannelSpec> channelSpecs = loadProfileSpec.getChannelSpecs();
        for (ChannelSpec channelSpec : channelSpecs) {
            channelTypes.remove(channelSpec.getChannelType().getId());
        }
        return Response.ok(PagedInfoList.fromPagedList("data", MeasurementTypeShortInfo.from(channelTypes.values()), queryParameters)).build();
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
        return masterDataService
                .findLoadProfileType(loadProfileTypeId)
                .orElseThrow(() -> new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND));
    }

    private LoadProfileSpec findLoadProfileSpecByIdOrThrowEception(long loadProfileSpecId) {
        return deviceConfigurationService
                .findLoadProfileSpec(loadProfileSpecId)
                .orElseThrow(() -> new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, loadProfileSpecId));
    }

    private ChannelSpec findChannelSpecByIdOrThrowException(long channelId) {
        return deviceConfigurationService
                .findChannelSpec(channelId)
                .orElseThrow(() -> new TranslatableApplicationException(thesaurus, MessageSeeds.NO_CHANNEL_SPEC_FOUND, channelId));
    }
}