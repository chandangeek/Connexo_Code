package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

@Path("/loadprofiles")
public class LoadProfileResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Thesaurus thesaurus;

    @Inject
    public LoadProfileResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus) {
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Path("/intervals")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIntervals(@BeanParam QueryParameters queryParameters){
        List<LocalizedTimeDuration.TimeDurationInfo> infos = new ArrayList<>(LocalizedTimeDuration.intervals.size());
        for (Map.Entry<Integer, LocalizedTimeDuration> timeDurationEntry : LocalizedTimeDuration.intervals.entrySet()) {
            LocalizedTimeDuration.TimeDurationInfo info = new LocalizedTimeDuration.TimeDurationInfo();
            info.id = timeDurationEntry.getKey();
            info.name = timeDurationEntry.getValue().toString(thesaurus);
            infos.add(info);
        }
        return Response.ok(infos).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProfileTypes(@BeanParam QueryParameters queryParameters) {
        List<LoadProfileType> allProfileTypes = masterDataService.findAllLoadProfileTypes();
        // Sorting
        Collections.sort(allProfileTypes, new Comparator<LoadProfileType>() {
            @Override
            public int compare(LoadProfileType o1, LoadProfileType o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        return Response.ok(PagedInfoList.asJson("data", LoadProfileTypeInfo.from(allProfileTypes), queryParameters)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLoadProfileType(@PathParam("id") long loadProfileId) {
        LoadProfileType loadProfileType = findLoadProfileByIdOrThrowException(loadProfileId);
        return Response.ok(LoadProfileTypeInfo.from(loadProfileType, isLoadProfileTypeAlreadyInUse(loadProfileType))).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addNewLoadProfileType(LoadProfileTypeInfo request, @Context UriInfo uriInfo) {
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(request.name, request.obisCode, request.timeDuration);
        boolean all = getBoolean(uriInfo, "all");
        if (all) {
            addAllChannelTypesToLoadProfileType(loadProfileType);
        } else {
            addChannelTypesToLoadProfileType(loadProfileType, request);
        }
        loadProfileType.save();
        return Response.ok(LoadProfileTypeInfo.from(loadProfileType, false)).build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editLoadProfileType(@PathParam("id") long loadProfileId, LoadProfileTypeInfo request, @Context UriInfo uriInfo) {
        LoadProfileType loadProfileType = findLoadProfileByIdOrThrowException(loadProfileId);
        loadProfileType.setName(request.name);
        boolean isInUse = isLoadProfileTypeAlreadyInUse(loadProfileType);
        if (!isInUse){
            loadProfileType.setInterval(request.timeDuration);
            loadProfileType.setObisCode(request.obisCode);
            boolean all = getBoolean(uriInfo, "all");
            if (all) {
                addAllChannelTypesToLoadProfileType(loadProfileType);
            } else {
                editChannelTypesToLoadProfileType(loadProfileType, request);
            }
        }
        loadProfileType.save();
        return Response.ok(LoadProfileTypeInfo.from(loadProfileType, isInUse)).build();
    }


    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProfileType(@PathParam("id") long loadProfileId) {
        findLoadProfileByIdOrThrowException(loadProfileId).delete();
        return Response.ok().build();
    }


    private LoadProfileType findLoadProfileByIdOrThrowException(long loadProfileId) {
        Optional<LoadProfileType> loadProfileTypeRef = masterDataService.findLoadProfileType(loadProfileId);
        if (!loadProfileTypeRef.isPresent()) {
            throw new TranslatableApplicationException(thesaurus, MessageSeeds.NO_LOAD_PROFILE_TYPE_FOUND, loadProfileId);
        }

        return loadProfileTypeRef.get();
    }


    private void addChannelTypesToLoadProfileType(LoadProfileType loadProfileType, LoadProfileTypeInfo request) {
        if (request.registerTypes != null) {
            for (RegisterTypeInfo registerTypeInfo : request.registerTypes) {
                Optional<RegisterType> registerType = masterDataService.findRegisterType(registerTypeInfo.id);
                if (registerType.isPresent()) {
                    loadProfileType.createChannelTypeForRegisterType(registerType.get());
                }
            }
        }
    }

    private void addAllChannelTypesToLoadProfileType(LoadProfileType loadProfileType) {
        Set<Long> alreadyAdded = new HashSet<>();
        for (ChannelType channelType : loadProfileType.getChannelTypes()) {
            alreadyAdded.add(channelType.getId());
        }
        for (RegisterType registerType : masterDataService.findAllRegisterTypes().find()) {
            if (!alreadyAdded.remove(registerType.getId())) {
                loadProfileType.createChannelTypeForRegisterType(registerType);
            }
        }
    }

    private void editChannelTypesToLoadProfileType(LoadProfileType loadProfileType, LoadProfileTypeInfo request) {
        if (request.registerTypes != null) {
            List<ChannelType> mappingsOnLoadProfile = loadProfileType.getChannelTypes();
            for (ChannelType channelType : mappingsOnLoadProfile) {
                loadProfileType.removeChannelType(channelType);
            }
            for (RegisterTypeInfo measurementType : request.registerTypes) {
                Optional<RegisterType> registerType = masterDataService.findRegisterType(measurementType.id);
                if (registerType.isPresent()) {
                    loadProfileType.createChannelTypeForRegisterType(registerType.get());
                }
            }
        }
    }
    private boolean isLoadProfileTypeAlreadyInUse(LoadProfileType loadProfileType){
        return !deviceConfigurationService.findDeviceConfigurationsUsingLoadProfileType(loadProfileType).isEmpty()
                || !deviceConfigurationService.findDeviceTypesUsingLoadProfileType(loadProfileType).isEmpty();
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }


}
