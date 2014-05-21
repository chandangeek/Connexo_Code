package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

public class LoadProfileTypeResource {

    private final ResourceHelper resourceHelper;
    private final MasterDataService masterDataService;
    private final Thesaurus thesaurus;

    @Inject
    public LoadProfileTypeResource(ResourceHelper resourceHelper, MasterDataService masterDataService, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.masterDataService = masterDataService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoadProfilesForDeviceType(@PathParam("id") long id, @BeanParam QueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LoadProfileType> loadProfileTypes = deviceType.getLoadProfileTypes();
        if (available != null && Boolean.parseBoolean(available)) {
            loadProfileTypes = findAllAvailableLoadProfileTypesForDeviceType(loadProfileTypes);
        }
        return Response.ok(PagedInfoList.asJson("data",
                LoadProfileTypeInfo.from(ListPager.of(loadProfileTypes, new LoadProfileTypeComparator()).find()), queryParameters)).build();
    }

    private List<LoadProfileType> findAllAvailableLoadProfileTypesForDeviceType(List<LoadProfileType> loadProfileTypes) {
        List<LoadProfileType> allLogBookTypes = masterDataService.findAllLoadProfileTypes();
        Set<Long> registeredLoadProfileTypeIds = new HashSet<>(loadProfileTypes.size());
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            registeredLoadProfileTypeIds.add(loadProfileType.getId());
        }
        Iterator<LoadProfileType> loadProfileTypeIterator = allLogBookTypes.iterator();
        while (loadProfileTypeIterator.hasNext()) {
            LoadProfileType loadProfileType = loadProfileTypeIterator.next();
            if (registeredLoadProfileTypeIds.contains(loadProfileType.getId())){
                loadProfileTypeIterator.remove();
            }
        }
        return allLogBookTypes;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLoadProfileTypesForDeviceType(@PathParam("id") long id, List<Long> ids) {
        if (ids.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(ids.size());
        for (Long loadProfileTypeId : ids) {
            Optional<LoadProfileType> loadPtofileTypeRef = masterDataService.findLoadProfileType(loadProfileTypeId);
            if (loadPtofileTypeRef.isPresent()){
                loadProfileTypes.add(loadPtofileTypeRef.get());
            }
        }
        for (LoadProfileType loadProfileType : loadProfileTypes) {
            deviceType.addLoadProfileType(loadProfileType);
        }
        return Response.ok(LoadProfileTypeInfo.from(loadProfileTypes)).build();
    }

    @DELETE
    @Path("/{loadProfileTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLoadProfileTypeFromDeviceType(
            @PathParam("id") long id,
            @PathParam("loadProfileTypeId") long loadProfileTypeId,
            @BeanParam QueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        LoadProfileType loadPtofileType = masterDataService.findLoadProfileType(loadProfileTypeId).orNull();
        if (loadPtofileType == null){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, loadProfileTypeId);
        }
        deviceType.removeLoadProfileType(loadPtofileType);
        return Response.ok().build();
    }
}
