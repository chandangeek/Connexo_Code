package com.elster.jupiter.transaction.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.transaction.TransactionService;

@Component (name = "com.elster.jupiter.datasource" )
public class TransactionalDataSource implements DataSource {
	
	private volatile TransactionServiceImpl transactionManager;
	
	public TransactionalDataSource() {
	}

	private DataSource getDataSource() {
		return transactionManager.getDataSource();
	}
	
	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return getDataSource().getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		getDataSource().setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		getDataSource().setLoginTimeout(seconds);

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return getDataSource().getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return getDataSource().getParentLogger();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return iface.isInstance(this) ? (T) this : getDataSource().unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return iface.isInstance(this) ? true : getDataSource().isWrapperFor(iface);
	}

	@Override
	public Connection getConnection() throws SQLException {		
		return transactionManager.getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {		
		throw new UnsupportedOperationException();
	}
	
	@Reference
	public void setTransactionService(TransactionService transactionService) {
		this.transactionManager = (TransactionServiceImpl) transactionService;
	}

}
