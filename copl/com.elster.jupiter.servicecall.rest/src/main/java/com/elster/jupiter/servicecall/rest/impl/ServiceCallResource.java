package com.elster.jupiter.servicecall.rest.impl;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;
import com.elster.jupiter.servicecall.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/servicecalls")
public class ServiceCallResource {
    private final ServiceCallService serviceCallService;
    private final ExceptionFactory exceptionFactory;
    private final ServiceCallInfoFactory serviceCallInfoFactory;

    @Inject
    public ServiceCallResource(ServiceCallService serviceCallService, ExceptionFactory exceptionFactory, ServiceCallInfoFactory serviceCallInfoFactory) {
        this.serviceCallService = serviceCallService;
        this.exceptionFactory = exceptionFactory;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public PagedInfoList getAllServiceCalls(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        Finder<ServiceCall> serviceCallFinder = serviceCallService.getServiceCallFinder(serviceCallInfoFactory.convertToServiceCallFilter(filter));
        List<ServiceCall> serviceCalls = serviceCallFinder.from(queryParameters).find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public ServiceCallInfo getServiceCall(@PathParam("id") long id) {
        return serviceCallService.getServiceCall(id)
                .map(serviceCall -> serviceCallInfoFactory.detailed(serviceCall, serviceCallService.getChildrenStatus(id)))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SERVICE_CALL));
    }

    @GET
    @Path("/{id}/children")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public PagedInfoList getChildren(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        ServiceCall parent = serviceCallService.getServiceCall(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL));
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();

        List<ServiceCall> serviceCalls = parent.findChildren(serviceCallInfoFactory.convertToServiceCallFilter(filter)).from(queryParameters).find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }
}
