package com.elster.jupiter.servicecall.rest.impl;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.impl.*;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFinder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.impl.ServiceCallInstaller;

import com.google.common.collect.Range;

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
import java.util.Optional;

@Path("/servicecalls")
public class ServiceCallResource {
    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ServiceCallResource(ServiceCallService serviceCallService, Thesaurus thesaurus, ExceptionFactory exceptionFactory) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllServiceCalls(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter filter) {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        //Finder<ServiceCall> serviceCallFinder = serviceCallService.getServiceCalls();
        ServiceCallFinder serviceCallFinder = serviceCallService.getServiceCallFinder();
        applyFilterToFinder(filter, serviceCallFinder);

        List<ServiceCall> serviceCalls = serviceCallFinder.find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(new ServiceCallInfo(serviceCall, thesaurus)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    private void applyFilterToFinder(JsonQueryFilter filter, ServiceCallFinder serviceCallFinder) {
        //TODO: SET START AND LIMIT
        //TODO: CHECK TYPE AND STATE REF ISSUE

        if(filter.hasProperty("number")) {
            serviceCallFinder.setReference(filter.getString("number"));
        }
        if(filter.hasProperty("type")) {
            //serviceCallFinder.setType(filter.getString("type"));
        }
        if(filter.hasProperty("state")) {
            //serviceCallFinder.setState(filter.getString("state"))
        }
        if(filter.hasProperty("receivedDateFrom")) {
            serviceCallFinder.withCreationTimeIn(Range.closed(filter.getInstant("receivedDateFrom"),
                    filter.hasProperty("receivedDateTo") ? filter.getInstant("receivedDateTo") : Instant.now()));
        } else if (filter.hasProperty("receivedDateTo")) {
            serviceCallFinder.withCreationTimeIn(Range.closed(Instant.EPOCH, filter.getInstant("receivedDateTo")));
        }
        if(filter.hasProperty("modificationDateFrom")) {
            serviceCallFinder.withModTimeIn(Range.closed(filter.getInstant("modificationDateFrom"),
                    filter.hasProperty("modificationDateTo") ? filter.getInstant("modificationDateTo") : Instant.now()));
        } else if (filter.hasProperty("modificationDateTo")) {
            serviceCallFinder.withModTimeIn(Range.closed(Instant.EPOCH, filter.getInstant("modificationDateTo")));
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public ServiceCallInfo getServiceCall(@PathParam("id") String number) {
        return serviceCallService.getServiceCall(number)
                .map(serviceCall -> new ServiceCallInfo(serviceCall, thesaurus))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_SERVICE_CALL));
    }

    @GET
    @Path("/{id}/children")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public PagedInfoList getChildren(@PathParam("id") String number, @BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallInfo> serviceCallInfos = new ArrayList<>();
        Finder<ServiceCall> serviceCallFinder = serviceCallService.getChildrenOf(number);
        List<ServiceCall> serviceCalls = serviceCallFinder.from(queryParameters).find();

        serviceCalls.stream()
                .forEach(serviceCall -> serviceCallInfos.add(new ServiceCallInfo(serviceCall, thesaurus)));

        return PagedInfoList.fromPagedList("serviceCalls", serviceCallInfos, queryParameters);
    }

    @PUT
    @Path("/{id}/cancel")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response cancelServiceCall(@PathParam("id") String number) {
        serviceCallService.getServiceCall(number).ifPresent(ServiceCall::cancel);
        return Response.status(Response.Status.OK).build();
    }

}
