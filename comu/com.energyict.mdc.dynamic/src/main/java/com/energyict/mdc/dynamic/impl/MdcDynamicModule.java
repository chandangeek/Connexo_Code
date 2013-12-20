package com.energyict.mdc.dynamic.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.impl.EnvironmentImpl;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.impl.RelationServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.sql.DataSource;

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

        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
    }

}