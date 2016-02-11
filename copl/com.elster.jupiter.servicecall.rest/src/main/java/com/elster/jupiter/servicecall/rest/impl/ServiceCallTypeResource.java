package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;

/**
 * Created by bvn on 2/11/16.
 */
public class ServiceCallTypeResource {

    private final ServiceCallService serviceCallService;

    @Inject
    public ServiceCallTypeResource(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }
}
