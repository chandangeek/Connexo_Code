package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;
import javax.ws.rs.Path;

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
}
