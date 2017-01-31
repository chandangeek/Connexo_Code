/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.examples.impl;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Example handler taking care of a disconnect
 * Created by bvn on 2/18/16.
 */
@Component(name = "com.elster.jupiter.servicecall.example.bogus.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=BogusHandler")
public class BogusHandler implements ServiceCallHandler {

    public BogusHandler() {
    }

    @Activate
    public void activate() {
        System.err.println("Activating bogus handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        System.out.println("Now entering state " + newState.getKey());
    }

    @Override
    public void onChildStateChange(ServiceCall parent, ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        System.out.println("Child " + serviceCall.getNumber() + " entering state " + newState.getKey());
    }
}
