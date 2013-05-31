package com.elster.jupiter.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;

import oracle.jdbc.pool.OracleDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

import javax.sql.DataSource;

import java.sql.SQLException;
import java.util.Properties;

@Component (name = "com.elster.jupiter.bootstrap")
public class BootstrapServiceImpl implements BootstrapService {

    private static final String JDBC_DRIVER_URL = "com.elster.jupiter.datasource.jdbcurl";
    private static final String JDBC_USER = "com.elster.jupiter.datasource.jdbcuser";
    private static final String JDBC_PASSWORD = "com.elster.jupiter.datasource.jdbcpassword";
    private static final String JDBC_POOLMAXLIMIT = "com.elster.jupiter.datasource.pool.maxlimit";
    private static final String JDBC_POOLMAXSTATEMENTS = "com.elster.jupiter.datasource.pool.maxstatements";

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private String maxLimit;
    private String maxStatementsLimit;

    public void activate(ComponentContext context) {
        jdbcUrl = getRequiredProperty(context.getBundleContext(), JDBC_DRIVER_URL);
        jdbcUser = getRequiredProperty(context.getBundleContext(), JDBC_USER);
        jdbcPassword = getRequiredProperty(context.getBundleContext(), JDBC_PASSWORD);
        maxLimit = getOptionalProperty(context.getBundleContext(), JDBC_POOLMAXLIMIT,"50");
        maxStatementsLimit = getOptionalProperty(context.getBundleContext(), JDBC_POOLMAXSTATEMENTS,"50");
    }

    @Override
    public DataSource createDataSource() throws SQLException {    	
    	return createDataSourceFromProperties();    	
    }

    // tried to switch to UCP , but ran into OSGI related class loading problems.
    // for now , we will live with deprecated methods
    @SuppressWarnings("deprecation")
	private DataSource createDataSourceFromProperties() throws SQLException {
        OracleDataSource source = new OracleDataSource();
        source.setURL(jdbcUrl);
        source.setUser(jdbcUser);
        source.setPassword(jdbcPassword);        
        Properties connectionCacheProps = new Properties();
        connectionCacheProps.put("MaxLimit",maxLimit);
        connectionCacheProps.put("MaxStatementsLimit", maxStatementsLimit);        
        source.setConnectionCacheProperties(connectionCacheProps);
        source.setConnectionCachingEnabled(true);
        // for now , no need to set connection properties , but possible interesting keys are
        // defaultRowPrefetch
        // oracle.jdbc.FreeMemoryOnEnterImplicitCache
        return source;
    }

    private String getRequiredProperty(BundleContext context, String property) {
        String value = context.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(property);
        }
        return value;
    }

    private String getOptionalProperty(BundleContext context, String property , String defaultValue) {
        String value = context.getProperty(property);
        return value == null ? defaultValue : value;
    }
}
