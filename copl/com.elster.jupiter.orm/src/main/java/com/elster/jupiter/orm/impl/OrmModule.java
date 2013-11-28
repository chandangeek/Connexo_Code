package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ServiceMatcher;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.sql.DataSource;

public class OrmModule extends AbstractModule {

    public OrmModule(TransactionInjection transactionInjection) {
        this.transactionInjection = transactionInjection;
    }

    public static interface TransactionInjection {
        void execute(Runnable runnable);
    }

    private final TransactionInjection transactionInjection;

    @Override
    protected void configure() {
        requireBinding(Clock.class);
        requireBinding(DataSource.class);
        requireBinding(JsonService.class);
        requireBinding(ThreadPrincipalService.class);

        bind(OrmService.class).to(OrmServiceImpl.class).in(Scopes.SINGLETON);
        bindListener(new ServiceMatcher(InstallService.class), new InstallerListener());
    }

    private class InstallerListener implements TypeListener {

        @Override
        public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
            encounter.register(new InjectionListener<I>() {
                @Override
                public void afterInjection(final I injectee) {
                    transactionInjection.execute(new Runnable() {
                        @Override
                        public void run() {
                            ((InstallService) injectee).install();
                        }
                    });
                }
            });
        }
    }
}
