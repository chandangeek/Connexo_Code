/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MeteringGroupsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(MeteringService.class);
        requireBinding(EventService.class);
        requireBinding(QueryService.class);
        requireBinding(NlsService.class);
        requireBinding(SearchService.class);

        bind(MeteringGroupsService.class).to(MeteringGroupsServiceImpl.class).in(Scopes.SINGLETON);
    }
}
