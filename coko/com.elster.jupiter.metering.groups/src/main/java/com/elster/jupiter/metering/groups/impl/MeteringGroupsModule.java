package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MeteringGroupsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(MessageService.class);

        bind(MeteringGroupsService.class).to(MeteringGroupsServiceImpl.class).in(Scopes.SINGLETON);

    }
}
