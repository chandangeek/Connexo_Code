package com.elster.jupiter.servicecall.rest.impl;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFinder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.security.Privileges;

import com.google.common.collect.Range;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
        //Finder<ServiceCall> serviceCallFinder = serviceCallService.getServiceCalls();
        ServiceCallFinder serviceCallFinder = serviceCallService.getServiceCallFinder();
        applyFilterToFinder(filter, serviceCallFinder);
        queryParameters.getLimit().ifPresent(limit -> serviceCallFinder.setLimit(limit + 1));
        queryParameters.getStart().ifPresent(start -> serviceCallFinder.setStart(start));
        List<ServiceCall> serviceCalls = serviceCallFinder.find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(serviceCallInfoFactory.from(serviceCall)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    private void applyFilterToFinder(JsonQueryFilter filter, ServiceCallFinder serviceCallFinder) {
        if (filter.hasProperty("number")) {
            serviceCallFinder.setReference(filter.getString("number"));
        }
        if (filter.hasProperty("type")) {
            serviceCallFinder.setType(filter.getStringList("type"));
        }
        if(filter.hasProperty("status")) {
            serviceCallFinder.setState(filter.getStringList("status"));
        }
        if (filter.hasProperty("receivedDateFrom")) {
            serviceCallFinder.withCreationTimeIn(Range.closed(filter.getInstant("receivedDateFrom"),
                    filter.hasProperty("receivedDateTo") ? filter.getInstant("receivedDateTo") : Instant.now()));
        } else if (filter.hasProperty("receivedDateTo")) {
            serviceCallFinder.withCreationTimeIn(Range.closed(Instant.EPOCH, filter.getInstant("receivedDateTo")));
        }
        if (filter.hasProperty("modificationDateFrom")) {
            serviceCallFinder.withModTimeIn(Range.closed(filter.getInstant("modificationDateFrom"),
                    filter.hasProperty("modificationDateTo") ? filter.getInstant("modificationDateTo") : Instant.now()));
        } else if (filter.hasProperty("modificationDateTo")) {
            serviceCallFinder.withModTimeIn(Range.closed(Instant.EPOCH, filter.getInstant("modificationDateTo")));
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public ServiceCallInfo getServiceCall(@PathParam("id") long number) {
        return serviceCallService.getServiceCall(number)
                .map(serviceCallInfoFactory::from)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SERVICE_CALL));
    }

    @GET
    @Path("/{id}/children")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public PagedInfoList getChildren(@PathParam("id") long number, @BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallInfo> serviceCallInfos = serviceCallService.getServiceCall(number)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL))
                .getChildren().from(queryParameters).stream()
                .map(serviceCallInfoFactory::from)
                .collect(toList());

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CHANGE_SERVICE_CALL_STATE)
    public Response cancelServiceCall(@PathParam("id") long number) {
        serviceCallService.getServiceCall(number).ifPresent(ServiceCall::cancel);
        return Response.status(Response.Status.OK).build();
    }

}
