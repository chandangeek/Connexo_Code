package com.elster.jupiter.servicecall.rest.impl;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.rest.whiteboard.ReferenceResolver;
import com.elster.jupiter.servicecall.*;
import com.elster.jupiter.servicecall.rest.ServiceCallInfo;
import com.elster.jupiter.servicecall.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/servicecalls")
public class ServiceCallResource {
    private final ServiceCallService serviceCallService;
    private final ExceptionFactory exceptionFactory;
    private final ServiceCallInfoFactory serviceCallInfoFactory;
    private final PropertyUtils propertyUtils;
    private final Thesaurus thesaurus;
    private final ReferenceResolver referenceResolver;

    @Inject
    public ServiceCallResource(ServiceCallService serviceCallService, ExceptionFactory exceptionFactory, ServiceCallInfoFactory serviceCallInfoFactory, PropertyUtils propertyUtils, Thesaurus thesaurus, ReferenceResolver referenceResolver) {
        this.serviceCallService = serviceCallService;
        this.exceptionFactory = exceptionFactory;
        this.serviceCallInfoFactory = serviceCallInfoFactory;
        this.propertyUtils = propertyUtils;
        this.thesaurus = thesaurus;
        this.referenceResolver = referenceResolver;
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
        if(info.state.id.equals("sclc.default.cancelled")) {
            serviceCallService.getServiceCall(id).ifPresent(ServiceCall::cancel);
            return Response.status(Response.Status.ACCEPTED).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CHANGE_SERVICE_CALL_STATE)
    public ServiceCallInfo postServiceCall(ServiceCallInfo info) {
        ServiceCallType serviceCallType = serviceCallService.getServiceCallTypes().stream()
                .filter(type -> type.getName().equals(info.type))
                .filter(type -> type.getVersionName().equals(info.typeVersionName))
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL_TYPE));

        ServiceCallBuilder builder = serviceCallType.newServiceCall()
                .externalReference(info.externalReference)
                .origin(info.origin);
        Optional.ofNullable(info.targetObject).ifPresent(builder::targetObject);
        ServiceCall serviceCall = builder
                .create();

        return new ServiceCallInfoFactory(thesaurus, propertyUtils, referenceResolver).summarized(serviceCall);
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.CHANGE_SERVICE_CALL_STATE)
    public Response deleteServiceCall(@PathParam("id") long id) {
        serviceCallService.getServiceCall(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL))
                .delete();
        return Response.status(Response.Status.OK).build();
    }
}
