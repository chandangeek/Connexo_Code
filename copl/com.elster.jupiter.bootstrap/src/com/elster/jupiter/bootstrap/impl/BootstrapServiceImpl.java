package com.elster.jupiter.bootstrap.impl;

import java.sql.SQLException;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.bootstrap.BootstrapService;

@Component (name = "com.elster.jupiter.bootstrap")
public class BootstrapServiceImpl implements BootstrapService {

	private volatile DataSource dataSource;
	
	public BootstrapServiceImpl() {		
	}
	
	@Override
	public DataSource getDataSource() {
		return dataSource;
	}
	
	@Activate 
	public void activate() throws SQLException {
		try {
			this.dataSource =  createDataSource();
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	private DataSource createDataSource() throws SQLException {
		OracleDataSource source = new OracleDataSource();
		source.setDriverType("thin");
		source.setServerName("localhost");
		source.setPortNumber(1521);
		source.setDatabaseName("eiserver");
		source.setUser("kore");
		source.setPassword("kore");	
		source.setConnectionCachingEnabled(true);
		return source;
	}
}
