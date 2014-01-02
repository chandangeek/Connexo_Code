package com.elster.jupiter.bootstrap.h2.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;

import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.inject.Inject;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

@Component(name = "com.elster.jupiter.bootstrap.h2", immediate = true)
public class H2BootstrapService implements BootstrapService {

    private static final String JDBC_URL_PATTERN = "jdbc:h2:mem:{0};MVCC=TRUE;lock_timeout={1}";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String DATABASE_NAME_BASE = "DB";
    
    private Connection connection;

    @Inject
    public H2BootstrapService() {
    }

    @Override
    public DataSource createDataSource() {
        String jdbcUrl = MessageFormat.format(JDBC_URL_PATTERN, DATABASE_NAME_BASE, String.valueOf(5000L));
        JdbcDataSource source = new JdbcDataSource();
        source.setURL(jdbcUrl);
        source.setUser(USER);
        source.setPassword(PASSWORD);
        try {
        	connection = source.getConnection();
        } catch (SQLException ex) {
        	throw new DataSourceSetupException(ex);
        }
        return new DecoratedDataSource(source);
    }
    

    @Deactivate
    public void deactivate() {
    	if (connection != null) {
    		try {
				connection.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
    	}
    }
}
