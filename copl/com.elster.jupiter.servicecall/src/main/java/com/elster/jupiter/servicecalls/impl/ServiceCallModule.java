package com.elster.jupiter.servicecalls.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.servicecalls.ServiceCallService;
import com.google.inject.AbstractModule;

/**
 * Created by bvn on 2/5/16.
 */
public class ServiceCallModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);

        bind(ServiceCallService.class).to(ServiceCallServiceImpl.class);
    }
}
