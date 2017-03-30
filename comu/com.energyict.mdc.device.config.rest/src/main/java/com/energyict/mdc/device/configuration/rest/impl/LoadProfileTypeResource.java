/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LoadProfileTypeResource {

    private final ResourceHelper resourceHelper;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;
    private final LoadProfileTypeOnDeviceTypeInfoFactory loadProfileTypeOnDeviceTypeInfoFactory;

    @Inject
    public LoadProfileTypeResource(ResourceHelper resourceHelper, MasterDataService masterDataService, Thesaurus thesaurus, LoadProfileTypeOnDeviceTypeInfoFactory loadProfileTypeOnDeviceTypeInfoFactory) {
        this.resourceHelper = resourceHelper;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
        this.loadProfileTypeOnDeviceTypeInfoFactory = loadProfileTypeOnDeviceTypeInfoFactory;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getLoadProfilesForDeviceType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LoadProfileType> loadProfileTypes = deviceType.getLoadProfileTypes();
        if (available != null && Boolean.parseBoolean(available)) {
            loadProfileTypes = findAllAvailableLoadProfileTypesForDeviceType(loadProfileTypes);
        }
        List<LoadProfileTypeOnDeviceTypeInfo> loadProfileTypeOnDeviceTypeInfos = new ArrayList<>();
        loadProfileTypes = ListPager.of(loadProfileTypes, new LoadProfileTypeComparator()).from(queryParameters).find();
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            loadProfileTypeOnDeviceTypeInfos.add(loadProfileTypeOnDeviceTypeInfoFactory.from(loadProfileType, deviceType));
        }
        return Response.ok(PagedInfoList.fromPagedList("data", loadProfileTypeOnDeviceTypeInfos, queryParameters)).build();
    }

    @GET @Transactional
    @Path("/{loadProfileTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public LoadProfileTypeOnDeviceTypeInfo getLoadProfileForDeviceType(@PathParam("id") long deviceTypeId,
                                                                       @PathParam("loadProfileTypeId") long loadProfileTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        LoadProfileType loadProfileType = resourceHelper.findLoadProfileTypeByIdOrThrowException(loadProfileTypeId);
        return loadProfileTypeOnDeviceTypeInfoFactory.from(loadProfileType, deviceType);
    }

    @PUT @Transactional
    @Path("/{loadProfileTypeId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response changeLoadProfileTypeOnDeviceTypeCustomPropertySet(@PathParam("id") long deviceTypeId,
                                                                       @PathParam("loadProfileTypeId") long loadProfileTypeId,
                                                                       LoadProfileTypeOnDeviceTypeInfo loadProfileTypeOnDeviceTypeInfo) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(deviceTypeId);
        LoadProfileType loadProfileType = resourceHelper.findLoadProfileTypeByIdOrThrowException(loadProfileTypeId);
        deviceType.addLoadProfileTypeCustomPropertySet(loadProfileType, loadProfileTypeOnDeviceTypeInfo.customPropertySet.id > 0 ?
                resourceHelper.findDeviceTypeCustomPropertySetByIdOrThrowException(loadProfileTypeOnDeviceTypeInfo.customPropertySet.id, ChannelSpec.class) : null);
        return Response.ok().build();
    }

    @GET @Transactional
    @Path("/custompropertysets")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_TYPE, Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getLoadProfileCustomPropertySets() {
        return Response.ok(DeviceTypeCustomPropertySetInfo.from(resourceHelper.findAllCustomPropertySetsByDomain(ChannelSpec.class))).build();
    }

    private List<LoadProfileType> findAllAvailableLoadProfileTypesForDeviceType(List<LoadProfileType> loadProfilesOnDeviceType) {
        Set<Long> alreadyAssignedLoadProfileTypes = loadProfilesOnDeviceType
                .stream()
                .map(LoadProfileType::getId)
                .collect(Collectors.toSet());
        return masterDataService.findAllLoadProfileTypes()
                .stream()
                .filter(candidate -> !alreadyAssignedLoadProfileTypes.contains(candidate.getId()))
                .collect(Collectors.toList());
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response addLoadProfileTypesForDeviceType(@PathParam("id") long id, List<Long> ids, @QueryParam("all") boolean all) {
        if (!all && ids.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Iterable<? extends LoadProfileType> toAdd = all ? allLoadProfileTypes() : loadProfileTypesFor(ids);
        for (LoadProfileType loadProfileType : toAdd) {
            deviceType.addLoadProfileType(loadProfileType);
        }
        return Response.ok(loadProfileTypeOnDeviceTypeInfoFactory.from(toAdd, deviceType)).build();
    }

    private Iterable<? extends LoadProfileType> allLoadProfileTypes() {
        return masterDataService.findAllLoadProfileTypes();
    }

    private Iterable<? extends LoadProfileType> loadProfileTypesFor(List<Long> ids) {
        return ids.stream()
                .map(id -> resourceHelper.findLoadProfileTypeByIdOrThrowException(id))
                .collect(Collectors.toList());
    }

    @DELETE @Transactional
    @Path("/{loadProfileTypeId}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteLoadProfileTypeFromDeviceType(
            @PathParam("id") long id,
            @PathParam("loadProfileTypeId") long loadProfileTypeId,
            @BeanParam JsonQueryParameters queryParameters,
            LoadProfileTypeOnDeviceTypeInfo info) {
        info.id = loadProfileTypeId;
        LoadProfileType loadProfileType = resourceHelper.lockDeviceTypeLoadProfileTypeOrThrowException(info);
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        deviceType.removeLoadProfileType(loadProfileType);
        return Response.ok().build();
    }
}
