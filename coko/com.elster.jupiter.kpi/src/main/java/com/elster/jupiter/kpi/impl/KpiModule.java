/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.kpi.KpiService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class KpiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KpiService.class).to(KpiServiceImpl.class).in(Scopes.SINGLETON);
    }
}
