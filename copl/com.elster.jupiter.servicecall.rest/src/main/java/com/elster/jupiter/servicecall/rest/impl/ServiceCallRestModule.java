/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.whiteboard.ReferenceResolver;
import com.elster.jupiter.servicecall.rest.ServiceCallInfoFactory;

import com.google.inject.AbstractModule;

public class ServiceCallRestModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(NlsService.class);
        requireBinding(ReferenceResolver.class);

        bind(ServiceCallInfoFactory.class).to(ServiceCallInfoFactoryImpl.class);
    }
}

