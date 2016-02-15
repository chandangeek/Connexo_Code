package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;


import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn on 2/11/16.
 */
@Path("/servicecalltypes")
public class ServiceCallTypeResource {

    private final ServiceCallService serviceCallService;
    private final Thesaurus thesaurus;

    @Inject
    public ServiceCallTypeResource(ServiceCallService serviceCallService, Thesaurus thesaurus) {
        this.serviceCallService = serviceCallService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllServiceCallTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallTypeInfo> serviceCallTypeInfos = new ArrayList<>();
        Finder<ServiceCallType> serviceCallTypeFinder = serviceCallService.getServiceCallTypes();

        List<ServiceCallType> allServiceCallTypes = serviceCallTypeFinder.from(queryParameters).find();
        allServiceCallTypes.stream()
                .forEach(sct -> serviceCallTypeInfos.add(new ServiceCallTypeInfo(sct, thesaurus)));

        return PagedInfoList.fromPagedList("serviceCallTypes", serviceCallTypeInfos, queryParameters);
    }

    @PUT
    @Path("/{serviceCallTypeName}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response changeLogLevel(@PathParam("serviceCallTypeID") String serviceCallTypeName, ServiceCallTypeInfo info) {
        ServiceCallType type = null; //= serviceCallService.getServiceCallType(serviceCallTypeName, info); lock and fetch
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

}
