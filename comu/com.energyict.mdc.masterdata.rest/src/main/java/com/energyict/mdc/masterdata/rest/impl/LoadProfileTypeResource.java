package com.energyict.mdc.masterdata.rest.impl;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfoFactory;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfo;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Path("/loadprofiles")
public class LoadProfileTypeResource {

    private final MasterDataService masterDataService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final Thesaurus thesaurus;
    private final ResourceHelper resourceHelper;
    private final RegisterTypeInfoFactory registerTypeInfoFactory;
    private final LoadProfileTypeInfoFactory loadProfileTypeInfoFactory;

    @Inject
    public LoadProfileTypeResource(MasterDataService masterDataService, DeviceConfigurationService deviceConfigurationService, Thesaurus thesaurus, ResourceHelper resourceHelper, RegisterTypeInfoFactory registerTypeInfoFactory, LoadProfileTypeInfoFactory loadProfileTypeInfoFactory) {
        this.masterDataService = masterDataService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
        this.resourceHelper = resourceHelper;
        this.registerTypeInfoFactory = registerTypeInfoFactory;
        this.loadProfileTypeInfoFactory = loadProfileTypeInfoFactory;
    }

    @GET
    @Transactional
    @Path("/intervals")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public Response getIntervals(@BeanParam JsonQueryParameters queryParameters) {
        return Response.ok(LocalizedTimeDuration.getAllInfos(thesaurus)).build();
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public Response getAllProfileTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<LoadProfileType> allProfileTypes = masterDataService.findAllLoadProfileTypes();

        allProfileTypes = ListPager.of(allProfileTypes, (lp1, lp2) -> lp1.getName().compareToIgnoreCase(lp2.getName())).from(queryParameters).find();
        return Response.ok(PagedInfoList.fromPagedList("data", loadProfileTypeInfoFactory.from(allProfileTypes), queryParameters)).build();
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_MASTER_DATA, Privileges.Constants.VIEW_MASTER_DATA})
    public Response getLoadProfileType(@PathParam("id") long loadProfileId) {
        LoadProfileType loadProfileType = resourceHelper.findLoadProfileTypeOrThrowException(loadProfileId);
        return Response.ok(loadProfileTypeInfoFactory.from(loadProfileType, isLoadProfileTypeAlreadyInUse(loadProfileType))).build();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public Response addNewLoadProfileType(LoadProfileTypeInfo request, @Context UriInfo uriInfo) {
        boolean all = getBoolean(uriInfo, "all");
        List<RegisterType> registerTypes = Collections.emptyList();
        if (all) {
            registerTypes =
                    masterDataService
                            .findAllRegisterTypes()
                            .find()
                            .stream()
                            .filter(readingTypesWithInterval())
                            .filter(filterOnCommodity())
                            .collect(Collectors.toList());
        } else {
            if (request.registerTypes != null) {
                registerTypes = request.registerTypes.stream().map(info -> masterDataService.findRegisterType(info.id)).flatMap(Functions.asStream()).collect(toList());
            }
        }
        LoadProfileType loadProfileType = masterDataService.newLoadProfileType(request.name, request.obisCode, request.timeDuration, registerTypes);
        loadProfileType.save();
        return Response.ok(loadProfileTypeInfoFactory.from(loadProfileType, false)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public Response editLoadProfileType(@PathParam("id") long loadProfileId, LoadProfileTypeInfo request, @Context UriInfo uriInfo) {
        LoadProfileType loadProfileType = resourceHelper.lockLoadProfileTypeOrThrowException(request);
        loadProfileType.setName(request.name);
        boolean isInUse = isLoadProfileTypeAlreadyInUse(loadProfileType);
        if (!isInUse) {
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
        return Response.ok(loadProfileTypeInfoFactory.from(loadProfileType, isInUse)).build();
    }


    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public Response deleteProfileType(@PathParam("id") long loadProfileId, LoadProfileTypeInfo request) {
        resourceHelper.lockLoadProfileTypeOrThrowException(request).delete();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/measurementtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public PagedInfoList getAvailableRegisterTypesForLoadProfileType(@BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters) {
        List<Long> excludedRegisterTypeIds = filter.getLongList("ids");
        Stream<RegisterType> registerTypeStream = this.masterDataService.findAllRegisterTypes().stream()
                .filter(readingTypesWithInterval())
                .filter(filterOnCommodity())
                .filter(registerType -> !excludedRegisterTypeIds.contains(registerType.getId()))
                .skip(queryParameters.getStart().get())
                .limit(queryParameters.getLimit().get() + 1);

        List<RegisterTypeInfo> registerTypeInfos = registerTypeStream
                .map(registerType -> registerTypeInfoFactory.asInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false))
                .collect(toList());
        return PagedInfoList.fromPagedList("registerTypes", registerTypeInfos, queryParameters);
    }

    @GET
    @Transactional
    @Path("{id}/measurementtypes")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_MASTER_DATA)
    public PagedInfoList getAvailableRegisterTypesForLoadProfileTypeById(@BeanParam JsonQueryFilter filter, @BeanParam JsonQueryParameters queryParameters, @PathParam("id") long loadProfileId) {
        LoadProfileType loadProfileType = resourceHelper.findLoadProfileTypeOrThrowException(loadProfileId);
        List<Long> excludedRegisterTypeIds = filter.getLongList("ids");
        Stream<RegisterType> registerTypeStream = this.masterDataService.findAllRegisterTypes().stream()
                .filter(readingTypesWithInterval())
                .filter(filterOnCommodity())
                .filter(filterExistingRegisterTypesOnLoadProfileType(loadProfileType))
                .filter(registerType -> !excludedRegisterTypeIds.contains(registerType.getId()))
                .skip(queryParameters.getStart().get())
                .limit(queryParameters.getLimit().get() + 1);

        List<RegisterTypeInfo> registerTypeInfos = registerTypeStream
                .map(registerType -> registerTypeInfoFactory.asInfo(registerType, this.deviceConfigurationService.isRegisterTypeUsedByDeviceType(registerType), false))
                .collect(toList());
        return PagedInfoList.fromPagedList("registerTypes", registerTypeInfos, queryParameters);
    }

    private Predicate<? super RegisterType> filterExistingRegisterTypesOnLoadProfileType(LoadProfileType loadProfileType) {
        return registerType -> loadProfileType.getChannelTypes().stream()
                .filter(channelType -> channelType.getTemplateRegister().getId() == registerType.getId()).count() == 0;
    }

    private Predicate<? super RegisterType> readingTypesWithInterval() {
        return registerType -> !(registerType.getReadingType().getMacroPeriod().isApplicable()
                || registerType.getReadingType().getMeasuringPeriod().isApplicable());
    }

    private Predicate<? super RegisterType> filterOnCommodity() {
        return registerType -> isItADataLoggerPulseReadingType(registerType) ||
                loadProfileCompatibleCommodities().contains(registerType.getReadingType().getCommodity());
    }

    private boolean isItADataLoggerPulseReadingType(RegisterType registerType) {
        return registerType.getReadingType().getMeasurementKind().equals(MeasurementKind.RELAYCYCLE);
    }

    private Set<Commodity> loadProfileCompatibleCommodities() {
        return EnumSet.complementOf(EnumSet.of(Commodity.NOTAPPLICABLE, Commodity.COMMUNICATION, Commodity.DEVICE));
    }

    private void addAllChannelTypesToLoadProfileType(LoadProfileType loadProfileType) {
        this.masterDataService
                .findAllRegisterTypes()
                .stream()
                .filter(filterExistingRegisterTypesOnLoadProfileType(loadProfileType))
                .forEach(loadProfileType::createChannelTypeForRegisterType);
    }

    private void editChannelTypesToLoadProfileType(LoadProfileType loadProfileType, LoadProfileTypeInfo request) {
        if (request.registerTypes != null) {
            loadProfileType.getChannelTypes().forEach(loadProfileType::removeChannelType);
            for (RegisterTypeInfo measurementType : request.registerTypes) {
                Optional<RegisterType> registerType = masterDataService.findRegisterType(measurementType.id);
                if (registerType.isPresent()) {
                    loadProfileType.createChannelTypeForRegisterType(registerType.get());
                }
            }
        }
    }

    private boolean isLoadProfileTypeAlreadyInUse(LoadProfileType loadProfileType) {
        return !deviceConfigurationService.findDeviceConfigurationsUsingLoadProfileType(loadProfileType).isEmpty()
                || !deviceConfigurationService.findDeviceTypesUsingLoadProfileType(loadProfileType).isEmpty();
    }

    private boolean getBoolean(UriInfo uriInfo, String key) {
        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        return queryParameters.containsKey(key) && Boolean.parseBoolean(queryParameters.getFirst(key));
    }

}
