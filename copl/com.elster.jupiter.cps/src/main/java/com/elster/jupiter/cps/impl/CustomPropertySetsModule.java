/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Module intended for use by integration tests.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-11 (12:46)
 */
public class CustomPropertySetsModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(TransactionService.class);
        requireBinding(UserService.class);
        requireBinding(SearchService.class);

        bind(CustomPropertySetService.class).to(CustomPropertySetServiceImpl.class).in(Scopes.SINGLETON);
    }

}