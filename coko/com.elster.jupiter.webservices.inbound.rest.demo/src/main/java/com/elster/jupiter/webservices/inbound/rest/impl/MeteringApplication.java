/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.inbound.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.ConstraintViolationInfo;
import com.elster.jupiter.rest.util.ExceptionFactory;

import com.google.common.collect.ImmutableSet;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

import javax.inject.Inject;
import javax.ws.rs.core.Application;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class MeteringApplication extends Application {

    private final MeteringService meteringService;

    @Inject
    public MeteringApplication(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public Set<Class<?>> getClasses() {
        return ImmutableSet.of(DeviceResource.class);
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> hashSet = new HashSet<>();
        hashSet.addAll(super.getSingletons());
        hashSet.add(new HK2Binder());
        return Collections.unmodifiableSet(hashSet);
    }

    class HK2Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(ConstraintViolationInfo.class).to(ConstraintViolationInfo.class);
            bind(meteringService).to(MeteringService.class);
            bind(ExceptionFactory.class).to(ExceptionFactory.class);
        }
    }
}