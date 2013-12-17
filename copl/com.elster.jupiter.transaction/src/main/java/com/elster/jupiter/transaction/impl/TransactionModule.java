package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import javax.sql.DataSource;

public class TransactionModule extends AbstractModule {
	
	public TransactionModule() {
	}
	
	public TransactionModule(boolean printSql) {
		Bus.printSql(printSql);
	}

    @Override
    protected void configure() {
        requireBinding(BootstrapService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Publisher.class);

        bind(TransactionService.class).to(TransactionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataSource.class).to(TransactionalDataSource.class).in(Scopes.SINGLETON);
    }
}
