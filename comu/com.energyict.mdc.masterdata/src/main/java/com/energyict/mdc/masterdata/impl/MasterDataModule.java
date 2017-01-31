/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.masterdata.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-04-11 (17:34)
 */
public class MasterDataModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(EventService.class);
        requireBinding(NlsService.class);
        requireBinding(MeteringService.class);
        requireBinding(MdcReadingTypeUtilService.class);

        bind(MasterDataService.class).to(MasterDataServiceImpl.class).in(Scopes.SINGLETON);
    }

}