package com.elster.jupiter.bootstrap.impl;

import org.osgi.framework.*;

import com.elster.jupiter.bootstrap.BootstrapService;

import oracle.jdbc.pool.OracleDataSource;
import java.sql.SQLException;

public class Activator implements BundleActivator {

	private volatile ServiceRegistration<BootstrapService> dataSourceRegistration;
	
	public void start(BundleContext context) throws SQLException  {
		OracleDataSource source = new OracleDataSource();
		source.setDriverType("thin");
		source.setServerName("localhost");
		source.setPortNumber(1521);
		source.setDatabaseName("eiserver");
		source.setUser("kore");
		source.setPassword("kore");	
		source.setConnectionCachingEnabled(true);
		dataSourceRegistration = context.registerService(BootstrapService.class,new BootstrapServiceImpl(source) ,null);
	}

	public void stop(BundleContext bundleContext) {
		dataSourceRegistration.unregister();
	}
}

