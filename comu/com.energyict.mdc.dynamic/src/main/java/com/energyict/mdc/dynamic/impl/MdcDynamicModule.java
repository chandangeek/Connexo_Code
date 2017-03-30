/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-19 (14:38)
 */
public class MdcDynamicModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(TransactionService.class);
        requireBinding(DataVaultService.class);
        requireBinding(com.elster.jupiter.properties.PropertySpecService.class);

        bind(PropertySpecService.class).to(PropertySpecServiceImpl.class).in(Scopes.SINGLETON);
    }

}