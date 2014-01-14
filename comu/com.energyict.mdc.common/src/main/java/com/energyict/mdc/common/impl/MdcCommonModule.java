package com.energyict.mdc.common.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.Environment;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.sql.DataSource;
import org.osgi.framework.BundleContext;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-09 (09:46)
 */
public class MdcCommonModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(BundleContext.class);
        requireBinding(DataSource.class);
        requireBinding(TransactionService.class);
        requireBinding(ThreadPrincipalService.class);

        bind(Environment.class).to(EnvironmentImpl.class).in(Scopes.SINGLETON);
    }

}