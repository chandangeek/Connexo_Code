/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.impl;

import com.elster.jupiter.audit.AuditService;
import com.elster.jupiter.orm.OrmService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class AuditModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(OrmService.class);
        ;

        bind(AuditService.class).to(AuditServiceImpl.class).in(Scopes.SINGLETON);
    }
}
