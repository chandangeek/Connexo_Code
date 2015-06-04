package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_TYPE, Privileges.VIEW_DEVICE_TYPE})
    public Response getLoadProfilesForDeviceType(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @QueryParam("available") String available) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        List<LoadProfileType> loadProfileTypes = deviceType.getLoadProfileTypes();
        if (available != null && Boolean.parseBoolean(available)) {
            loadProfileTypes = findAllAvailableLoadProfileTypesForDeviceType(loadProfileTypes);
        }
        loadProfileTypes = ListPager.of(loadProfileTypes, new LoadProfileTypeComparator()).from(queryParameters).find();
        return Response.ok(PagedInfoList.fromPagedList("data", LoadProfileTypeInfo.from(loadProfileTypes), queryParameters)).build();
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
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response addLoadProfileTypesForDeviceType(@PathParam("id") long id, List<Long> ids, @Context UriInfo uriInfo) {
        boolean all = getBoolean(uriInfo, "all");
        if (!all && ids.isEmpty()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_ID_FOR_ADDING);
        }
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        Iterable<? extends LoadProfileType> toAdd = all ? allLoadProfileTypes() : loadProfileTypesFor(ids);
        for (LoadProfileType loadProfileType : toAdd) {
            deviceType.addLoadProfileType(loadProfileType);
        }
        return Response.ok(LoadProfileTypeInfo.from(toAdd)).build();
    }

    private Iterable<? extends LoadProfileType> allLoadProfileTypes() {
        return masterDataService.findAllLoadProfileTypes();
    }

    private Iterable<? extends LoadProfileType> loadProfileTypesFor(List<Long> ids) {
        return Iterables.transform(ids, new Function<Long, LoadProfileType>() {
            @Override
            public LoadProfileType apply(Long id) {
                Optional<LoadProfileType> loadProfileType = masterDataService.findLoadProfileType(id);
                if (loadProfileType.isPresent()) {
                    return loadProfileType.get();
                }
                throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, id);
            }
        });
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

    @DELETE
    @Path("/{loadProfileTypeId}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_TYPE)
    public Response deleteLoadProfileTypeFromDeviceType(
            @PathParam("id") long id,
            @PathParam("loadProfileTypeId") long loadProfileTypeId,
            @BeanParam JsonQueryParameters queryParameters) {
        DeviceType deviceType = resourceHelper.findDeviceTypeByIdOrThrowException(id);
        LoadProfileType loadPtofileType = masterDataService.findLoadProfileType(loadProfileTypeId).orElse(null);
        if (loadPtofileType == null){
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, loadProfileTypeId);
        }
        deviceType.removeLoadProfileType(loadPtofileType);
        return Response.ok().build();
    }
}
