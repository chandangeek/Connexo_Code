/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 3/2/16.
 */
@Path("/servicecalls/{serviceCallId}/logs")
public class ServiceCallLogResource {
    private final ServiceCallService serviceCallService;
    private final ExceptionFactory exceptionFactory;
    private final ServiceCallLogInfoFactory serviceCallLogInfoFactory;

    @Inject
    public ServiceCallLogResource(ServiceCallService serviceCallService, ExceptionFactory exceptionFactory, ServiceCallLogInfoFactory serviceCallLogInfoFactory) {
        this.serviceCallService = serviceCallService;
        this.exceptionFactory = exceptionFactory;
        this.serviceCallLogInfoFactory = serviceCallLogInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.VIEW_SERVICE_CALLS)
    public PagedInfoList getAllLogs(@PathParam("serviceCallId") long serviceCallId, @BeanParam JsonQueryParameters queryParameters) {
        ServiceCall serviceCall = serviceCallService.getServiceCall(serviceCallId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_SERVICE_CALL));

        List<ServiceCallLogInfo> serviceCallLogs = serviceCall.getLogs()
                .from(queryParameters)
                .stream()
                .map(serviceCallLogInfoFactory::from)
                .collect(toList());
        return PagedInfoList.fromPagedList("logs", serviceCallLogs, queryParameters);
    }
}
