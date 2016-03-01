package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.security.Privileges;


import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bvn on 2/11/16.
 */
@Path("/servicecalltypes")
public class ServiceCallTypeResource {

    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    public ServiceCallTypeResource(ServiceCallService serviceCallService, Thesaurus thesaurus, ConcurrentModificationExceptionFactory conflictFactory) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.conflictFactory = conflictFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALL_TYPES, Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES})
    public PagedInfoList getAllServiceCallTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallTypeInfo> serviceCallTypeInfos = new ArrayList<>();
        Finder<ServiceCallType> serviceCallTypeFinder = serviceCallService.getServiceCallTypes();

        List<ServiceCallType> allServiceCallTypes = serviceCallTypeFinder.from(queryParameters).find();
        Comparator<ServiceCallType> comparator = Comparator.comparing(ServiceCallType::getName);
        comparator = comparator.thenComparing(ServiceCallType::getVersionName);
        allServiceCallTypes.stream()
                .sorted(comparator)
                .forEach(type -> serviceCallTypeInfos.add(new ServiceCallTypeInfo(type, thesaurus)));

        return PagedInfoList.fromPagedList("serviceCallTypes", serviceCallTypeInfos, queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES})
    public Response changeLogLevel(@PathParam("id") long id, ServiceCallTypeInfo info) {
        ServiceCallType type = fetchAndLockServiceCallType(info);
        type.setLogLevel(LogLevel.valueOf(info.logLevel.id));
        type.save();
        return Response.status(Response.Status.OK).build();
    }

    private ServiceCallType fetchAndLockServiceCallType(ServiceCallTypeInfo info) {
        return serviceCallService.findAndLockServiceCallType(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> serviceCallService.findServiceCallType(info.name, info.versionName).map(ServiceCallType::getVersion).orElse(null))
                        .supplier());
    }

}
