package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;


import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bvn on 2/11/16.
 */
@Path("/servicecalltypes")
public class ServiceCallTypeResource {

    private final ServiceCallService serviceCallService;

    @Inject
    public ServiceCallTypeResource(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllServiceCallTypes(@BeanParam JsonQueryParameters queryParameters) {
        List<ServiceCallTypeInfo> serviceCallTypeInfos = new ArrayList<>();
        Finder<ServiceCallType> serviceCallTypeFinder = serviceCallService.getServiceCallTypes();

        List<ServiceCallType> allServiceCallTypes = serviceCallTypeFinder.from(queryParameters).find();
        allServiceCallTypes.stream()
                .forEach(sct -> serviceCallTypeInfos.add(new ServiceCallTypeInfo(sct)));

        return PagedInfoList.fromPagedList("serviceCallTypes", serviceCallTypeInfos, queryParameters);
    }
}
