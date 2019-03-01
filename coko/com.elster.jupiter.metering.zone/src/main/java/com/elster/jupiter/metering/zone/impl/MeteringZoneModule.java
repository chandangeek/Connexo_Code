/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.zone.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.zone.MeteringZoneService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.UpgradeService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class MeteringZoneModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(UpgradeService.class);
        requireBinding(MeteringService.class);

        bind(MeteringZoneService.class).to(MeteringZoneServiceImpl.class).in(Scopes.SINGLETON);
    }
}
