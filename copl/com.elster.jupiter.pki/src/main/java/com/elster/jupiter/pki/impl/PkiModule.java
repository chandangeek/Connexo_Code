/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.users.UserService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * Created by bvn on 1/26/17.
 */
public class PkiModule extends AbstractModule {
    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        requireBinding(NlsService.class);
        requireBinding(PropertySpecService.class);
        requireBinding(UserService.class);

        bind(SecurityManagementService.class).to(SecurityManagementServiceImpl.class).in(Scopes.SINGLETON);
        bind(CaService.class).to(CaServiceImpl.class).in(Scopes.SINGLETON);
    }

}
