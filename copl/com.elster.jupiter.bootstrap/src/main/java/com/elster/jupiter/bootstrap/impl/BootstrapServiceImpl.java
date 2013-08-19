package com.elster.jupiter.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import oracle.jdbc.pool.OracleDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This Component is responsible for creating a DataSource on demand. It does so by getting the needed properties from the BundleContext.
 * <br/>
 * Required properties :
 * <ul>
 * <li><code>com.elster.jupiter.datasource.jdbcurl</code> : the database url.</li>
 * <li><code>com.elster.jupiter.datasource.jdbcuser</code> : the database user.</li>
 * <li><code>com.elster.jupiter.datasource.jdbcpassword</code> : the database user's password.</li>
 * </ul>
 * Optional properties :
 * <ul>
 * <li><code>com.elster.jupiter.datasource.pool.maxlimit</code> : max limit, will default to 50.</li>
 * <li><code>com.elster.jupiter.datasource.pool.maxstatements</code> : max statements, will default to 50.</li>
 * </ul>
 */
@Component(name = "com.elster.jupiter.bootstrap")
public class BootstrapServiceImpl implements BootstrapService {

    private static final String JDBC_DRIVER_URL = "com.elster.jupiter.datasource.jdbcurl";
    private static final String JDBC_USER = "com.elster.jupiter.datasource.jdbcuser";
    private static final String JDBC_PASSWORD = "com.elster.jupiter.datasource.jdbcpassword";
    private static final String JDBC_POOLMAXLIMIT = "com.elster.jupiter.datasource.pool.maxlimit";
    private static final String JDBC_POOLMAXSTATEMENTS = "com.elster.jupiter.datasource.pool.maxstatements";
    private static final String MAX_LIMIT = "MaxLimit";
    private static final String MAX_STATEMENTS_LIMIT = "MaxStatementsLimit";

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private String maxLimit;
    private String maxStatementsLimit;

    @Activate
    public void activate(BundleContext context) {
        jdbcUrl = getRequiredProperty(context, JDBC_DRIVER_URL);
        jdbcUser = getRequiredProperty(context, JDBC_USER);
        jdbcPassword = getRequiredProperty(context, JDBC_PASSWORD);
        maxLimit = getOptionalProperty(context, JDBC_POOLMAXLIMIT, "50");
        maxStatementsLimit = getOptionalProperty(context, JDBC_POOLMAXSTATEMENTS, "50");
    }

    @Override
    public DataSource createDataSource() {
        try {
            return createDataSourceFromProperties();
        } catch (SQLException e) {
            // Basically this should never occur, since we're not accessing the DB in any way, just yet.
            throw new RuntimeException(e);
        }
    }

    // tried to switch to UCP , but ran into OSGI related class loading problems.
    // for now , we will live with deprecated methods
    @SuppressWarnings("deprecation")
    private DataSource createDataSourceFromProperties() throws SQLException {
        OracleDataSource source = new OracleDataSource();
        source.setURL(jdbcUrl);
        source.setUser(jdbcUser);
        source.setPassword(jdbcPassword);
        source.setConnectionCacheProperties(connectionCacheProperties());
        source.setConnectionCachingEnabled(true);
        // for now , no need to set connection properties , but possible interesting keys are
        // defaultRowPrefetch
        // oracle.jdbc.FreeMemoryOnEnterImplicitCache
        return source;
    }

    private Properties connectionCacheProperties() {
        Properties connectionCacheProps = new Properties();
        connectionCacheProps.put(MAX_LIMIT, maxLimit);
        connectionCacheProps.put(MAX_STATEMENTS_LIMIT, maxStatementsLimit);
        return connectionCacheProps;
    }

    private String getRequiredProperty(BundleContext context, String property) {
        String value = context.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(property);
        }
        return value;
    }

    private String getOptionalProperty(BundleContext context, String property, String defaultValue) {
        String value = context.getProperty(property);
        return value == null ? defaultValue : value;
    }
}
