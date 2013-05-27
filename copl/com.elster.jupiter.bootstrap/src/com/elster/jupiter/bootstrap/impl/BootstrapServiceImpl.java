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

@Component (name = "com.elster.jupiter.bootstrap")
public class BootstrapServiceImpl implements BootstrapService {

    private static final String JDBC_DRIVER_URL = "com.elster.jupiter.datasource.jdbcurl";
    private static final String JDBC_USER = "com.elster.jupiter.datasource.jdbcuser";
    private static final String JDBC_PASSWORD = "com.elster.jupiter.datasource.jdbcpassword";

    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;

    @Activate
    public void activate(BundleContext context) {
        jdbcUrl = getRequiredProperty(context, JDBC_DRIVER_URL);
        jdbcUser = getRequiredProperty(context, JDBC_USER);
        jdbcPassword = getRequiredProperty(context, JDBC_PASSWORD);
    }

    @Override
    public DataSource createDataSource() throws SQLException {
        return createDataSourceFromProperties(new Properties());
    }

    private DataSource createDataSourceFromProperties(Properties properties) throws SQLException {
        OracleDataSource source = new OracleDataSource();
        source.setURL(jdbcUrl);
        source.setUser(jdbcUser);
        source.setPassword(jdbcPassword);
        source.setConnectionCachingEnabled(true);
        return source;
    }

    private String getRequiredProperty(BundleContext context, String property) {
        String value = context.getProperty(property);
        if (value == null) {
            throw new PropertyNotFoundException(property);
        }
        return value;
    }

}
