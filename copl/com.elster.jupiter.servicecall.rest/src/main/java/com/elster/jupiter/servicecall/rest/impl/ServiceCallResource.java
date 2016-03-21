package com.elster.jupiter.servicecall.rest.impl;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;
import com.elster.jupiter.servicecall.ServiceCallService;
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
    private final PropertyUtils propertyUtils;

    @Inject
    public ServiceCallResource(ServiceCallService serviceCallService, ExceptionFactory exceptionFactory, ServiceCallInfoFactory serviceCallInfoFactory, PropertyUtils propertyUtils) {
        this.serviceCallService = serviceCallService;
        this.exceptionFactory = exceptionFactory;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
        this.propertyUtils = propertyUtils;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public PagedInfoList getAllServiceCalls(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        Finder<ServiceCall> serviceCallFinder = serviceCallService.getServiceCallFinder(convertToFilter(filter));
        List<ServiceCall> serviceCalls = serviceCallFinder.from(queryParameters).find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    private ServiceCallFilter convertToFilter(JsonQueryFilter filter) {
        ServiceCallFilter serviceCallFilter = serviceCallService.newServiceCallFilter();
        if (filter.hasProperty("name")) {
            serviceCallFilter.setReference(filter.getString("name"));
        }
        if (filter.hasProperty("type")) {
            serviceCallFilter.setTypes(filter.getStringList("type"));
        }
        if(filter.hasProperty("status")) {
            serviceCallFilter.setStates(filter.getStringList("status"));
        }
        if (filter.hasProperty("receivedDateFrom")) {
            serviceCallFilter.setReceivedDateFrom(filter.getInstant("receivedDateFrom"));
        } else if (filter.hasProperty("receivedDateTo")) {
            serviceCallFilter.setReceivedDateTo(filter.getInstant("receivedDateTo"));
        }
        if (filter.hasProperty("modificationDateFrom")) {
            serviceCallFilter.setModificationDateFrom(filter.getInstant("modificationDateFrom"));
        } else if (filter.hasProperty("modificationDateTo")) {
            serviceCallFilter.setModificationDateTo(filter.getInstant("modificationDateTo"));
        }

        return serviceCallFilter;
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

        List<ServiceCall> serviceCalls = parent.findChildren(convertToFilter(filter)).from(queryParameters).find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.summarized(serviceCall)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CHANGE_SERVICE_CALL_STATE)
    public Response cancelServiceCall(@PathParam("id") long id, ServiceCallInfo info) {
        if(info.state.equals("Cancelled")) {
            serviceCallService.getServiceCall(id).ifPresent(ServiceCall::cancel);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
