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


import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

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
    public PagedInfoList getAllServiceCallTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallTypeInfo> serviceCallTypeInfos = new ArrayList<>();
        Finder<ServiceCallType> serviceCallTypeFinder = serviceCallService.getServiceCallTypes();

        List<ServiceCallType> allServiceCallTypes = serviceCallTypeFinder.from(queryParameters).find();
        allServiceCallTypes.stream()
                .forEach(type -> serviceCallTypeInfos.add(new ServiceCallTypeInfo(type, thesaurus)));

        return PagedInfoList.fromPagedList("serviceCallTypes", serviceCallTypeInfos, queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response changeLogLevel(@PathParam("id") long id, ServiceCallTypeInfo info) {
        ServiceCallType type = fetchAndLockAppServer(info);
        type.setLogLevel(LogLevel.valueOf(info.logLevel.id));
        type.save();
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("/{serviceCallTypeName}/cancel")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response cancelServiceCall(@PathParam("serviceCallTypeID") String serviceCallTypeName, ServiceCallTypeInfo info) {

        return Response.status(Response.Status.OK).build();
    }

    private ServiceCallType fetchAndLockAppServer(ServiceCallTypeInfo info) {
        return serviceCallService.findAndLockServiceCallType(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> serviceCallService.findServiceCallType(info.name, info.versionName).map(ServiceCallType::getVersion).orElse(null))
                        .supplier());
    }

}
