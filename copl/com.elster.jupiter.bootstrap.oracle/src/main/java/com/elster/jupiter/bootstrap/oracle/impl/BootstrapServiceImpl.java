package com.elster.jupiter.bootstrap.oracle.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;
import com.elster.jupiter.bootstrap.PropertyNotFoundException;
import oracle.jdbc.pool.OracleDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
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
@Component(name = "com.elster.jupiter.bootstrap.oracle",
        property = {"osgi.command.scope=orm", "osgi.command.function=dbConnection"})
public final class BootstrapServiceImpl implements BootstrapService {

    /**
     * see https://docs.oracle.com/cd/B28359_01/java.111/b31224/concache.htm for doc on oracle implicit connection caching
     */
    private static final String MAX_LIMIT = "MaxLimit";
    private static final String MAX_STATEMENTS_LIMIT = "MaxStatementsLimit";
    private static final String CONNECTION_WAIT_TIMEOUT = "ConnectionWaitTimeout";
    static final String ORACLE_CONNECTION_POOL_NAME = "OracleConnectionPool";
    private static final String VALIDATE_CONNECTIONS = "ValidateConnection";

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private String maxLimit;
    private String maxStatementsLimit;

    public BootstrapServiceImpl() {
    }

    @Inject
    public BootstrapServiceImpl(BundleContext bundleContext) {
        activate(bundleContext);
    }

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
            throw new DataSourceSetupException(e);
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
        source.setConnectionCachingEnabled(true);
        source.setConnectionCacheProperties(connectionCacheProperties());
        source.setConnectionCacheName(ORACLE_CONNECTION_POOL_NAME);
        // for now , no need to set connection properties , but possible interesting keys are
        // defaultRowPrefetch
        // oracle.jdbc.FreeMemoryOnEnterImplicitCache

        return source;
    }

    private Properties connectionCacheProperties() {
        Properties connectionCacheProps = new Properties();
        connectionCacheProps.put(MAX_LIMIT, maxLimit);
        connectionCacheProps.put(MAX_STATEMENTS_LIMIT, maxStatementsLimit);
        connectionCacheProps.put(CONNECTION_WAIT_TIMEOUT, "10"); // 10 seconds
        connectionCacheProps.put(VALIDATE_CONNECTIONS, "true");
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

    public void dbConnection() {
        StringBuilder sb = new StringBuilder("Connection settings :").append("\n");
        sb.append(" jdbcUrl = ").append(jdbcUrl).append("\n");
        sb.append(" dbUser = ").append(jdbcUser).append("\n");
        System.out.println(sb.toString());
    }
}
