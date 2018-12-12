/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.groups.impl;

import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.yellowfin.groups.YellowfinGroupsService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class YellowfinGroupsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(MessageService.class);

        bind(YellowfinGroupsService.class).to(YellowfinGroupsServiceImpl.class).in(Scopes.SINGLETON);

    }
}
