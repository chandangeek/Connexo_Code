package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionModule extends AbstractModule {

    private Connection lifeLineConnection;

    @Override
    protected void configure() {
        requireBinding(BootstrapService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Publisher.class);

        bind(TransactionService.class).to(TransactionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataSource.class).to(TransactionalDataSource.class).in(Scopes.SINGLETON);

        bindListener(new ServiceMatcher(DataSource.class), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register(new InjectionListener<I>() {
                    @Override
                    public void afterInjection(I injectee) {
                        try {
                            lifeLineConnection = ((DataSource) injectee).getConnection();
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });

    }

    public void closeLifeLineConnection() {
        if (lifeLineConnection != null) {
            try {
                lifeLineConnection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
