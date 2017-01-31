/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;
import com.elster.jupiter.servicecall.security.Privileges;

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
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/servicecalltypes")
public class ServiceCallTypeResource {

    private final ServiceCallService serviceCallService;
    private final ServiceCallTypeInfoFactory serviceCallTypeInfoFactory;
    private final ConcurrentModificationExceptionFactory conflictFactory;
    private final CustomPropertySetService customPropertySetService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ServiceCallTypeResource(ServiceCallService serviceCallService,
                                   ServiceCallTypeInfoFactory serviceCallTypeInfoFactory,
                                   ConcurrentModificationExceptionFactory conflictFactory,
                                   CustomPropertySetService customPropertySetService,
                                   ExceptionFactory exceptionFactory) {
        this.serviceCallService = serviceCallService;
        this.serviceCallTypeInfoFactory = serviceCallTypeInfoFactory;
        this.conflictFactory = conflictFactory;
        this.customPropertySetService = customPropertySetService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_SERVICE_CALL_TYPES, Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES, Privileges.Constants.VIEW_SERVICE_CALLS})
    public PagedInfoList getAllServiceCallTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallTypeInfo> serviceCallTypeInfos = serviceCallService.getServiceCallTypes()
                .from(queryParameters)
                .stream()
                .map(serviceCallTypeInfoFactory::from)
                .collect(toList());

        return PagedInfoList.fromPagedList("serviceCallTypes", serviceCallTypeInfos, queryParameters);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES})
    public Response changeLogLevel(@PathParam("id") long id, ServiceCallTypeInfo info) {
        info.id = id; // oh well
        ServiceCallType type = fetchAndLockServiceCallType(info);
        if (info.logLevel != null) {
            type.setLogLevel(LogLevel.valueOf(info.logLevel.id));
        } else {
            type.setLogLevel(null);
        }
        type.save();
        return Response.ok(serviceCallTypeInfoFactory.from(type)).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES})
    public ServiceCallTypeInfo postServiceCallType(ServiceCallTypeInfo info) {
        ServiceCallLifeCycle serviceCallLifeCycle = serviceCallService.getServiceCallLifeCycles().stream()
                .filter(scl -> Long.valueOf(info.serviceCallLifeCycle.id.toString()).equals(scl.getId()))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL_LIFE_CYCLE));
        ServiceCallTypeBuilder builder = serviceCallService.createServiceCallType(info.name, info.versionName, serviceCallLifeCycle);
        builder.handler(info.handler);
        builder.logLevel(LogLevel.valueOf(info.logLevel.id));
        info.customPropertySets
                .forEach(cps -> builder.customPropertySet(customPropertySetService.findActiveCustomPropertySet(cps.name)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_CUSTOM_ATTRIBUTE_SET))));
        return serviceCallTypeInfoFactory.from(builder.create());
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_SERVICE_CALL_TYPES})
    public Response deleteServiceCallType(@PathParam("id") long id, ServiceCallTypeInfo info) {
        serviceCallService.findServiceCallType(info.name, info.versionName)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SERVICE_CALL_TYPE))
                .delete();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private ServiceCallType fetchAndLockServiceCallType(ServiceCallTypeInfo info) {
        return serviceCallService.findAndLockServiceCallType(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> serviceCallService.findServiceCallType(info.name, info.versionName).map(ServiceCallType::getVersion).orElse(null))
                        .supplier());
    }
}
