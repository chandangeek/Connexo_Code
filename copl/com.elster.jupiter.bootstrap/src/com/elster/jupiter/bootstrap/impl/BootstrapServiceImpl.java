package com.elster.jupiter.bootstrap.impl;

import java.sql.SQLException;
import javax.sql.DataSource;
import oracle.jdbc.pool.OracleDataSource;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.bootstrap.BootstrapService;

@Component (name = "com.elster.jupiter.bootstrap")
public class BootstrapServiceImpl implements BootstrapService {
	
	public BootstrapServiceImpl() {		
	}
	
	@SuppressWarnings("deprecation")
	public DataSource createDataSource() throws SQLException {
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
