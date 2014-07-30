package com.elster.jupiter.kpi.impl;

import com.elster.jupiter.kpi.KpiService;
import com.google.inject.AbstractModule;

/**
 * Copyrights EnergyICT
 * Date: 30/07/2014
 * Time: 9:22
 */
public class KpiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KpiService.class).to(KpiServiceImpl.class);

    }
}
