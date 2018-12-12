/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecall.ServiceCallService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Created by bvn on 2/5/16.
 */
public class ServiceCallModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(ServiceCallService.class).to(IServiceCallService.class);
        bind(IServiceCallService.class).to(ServiceCallServiceImpl.class).in(Scopes.SINGLETON);
    }
}
