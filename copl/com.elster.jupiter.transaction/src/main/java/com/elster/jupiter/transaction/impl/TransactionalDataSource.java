/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction.impl;

import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

@Component (name = "com.elster.jupiter.datasource" )
public class TransactionalDataSource implements DataSource {
	private volatile TransactionServiceImpl transactionService;
	
	public TransactionalDataSource() {
	}

    @Inject
    public TransactionalDataSource(TransactionService transactionService) {
        this.transactionService = (TransactionServiceImpl) transactionService;
    }

    @Override
	public Connection getConnection() throws SQLException {		
		return new MonitoredConnection(transactionService);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {		
		throw new UnsupportedOperationException();
	}
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return getDataSource().getLogWriter();
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return getDataSource().getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return getDataSource().getParentLogger();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this) || getDataSource().isWrapperFor(iface);
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		getDataSource().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		getDataSource().setLoginTimeout(seconds);
	}
	
	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = (TransactionServiceImpl) transactionService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.isInstance(this) ? (T) this : getDataSource().unwrap(iface);
	}

	private DataSource getDataSource() {
		return transactionService.getDataSource();
	}
}
