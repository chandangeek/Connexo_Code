package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.sql.DataSource;

public class TransactionModule extends AbstractModule implements TransactionServiceImpl.TransactionServiceConfig {
	
	private final boolean printSql;
	
	public TransactionModule() {
		this(false);
	}
	
	public TransactionModule(boolean printSql) {
		this.printSql = printSql;
	}

    @Override
    protected void configure() {
        requireBinding(BootstrapService.class);
        requireBinding(ThreadPrincipalService.class);
        requireBinding(Publisher.class);
        bind(TransactionServiceImpl.TransactionServiceConfig.class).toInstance(this);
        bind(TransactionService.class).to(TransactionServiceImpl.class).in(Scopes.SINGLETON);
        bind(DataSource.class).to(TransactionalDataSource.class).in(Scopes.SINGLETON);
    }

	@Override
	public boolean printSql() {
		return printSql;
	}
}
