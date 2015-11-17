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
    private DecoratedDataSource decoratedDataSource;

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
        decoratedDataSource = new DecoratedDataSource(source);

        try (Connection connection = decoratedDataSource.getConnection()){
            connection.prepareStatement("create alias regexp_like as $$ boolean regexpLike(String s, String p, String ignore) { return s.matches(p); } $$;").execute();
        } catch (SQLException e) {
            throw new DataSourceSetupException(e);
        }
        return decoratedDataSource;
    }


    @Deactivate
    public void deactivate() {
    	if (decoratedDataSource == null) {
    		return;
    	}
        try {
            if (!decoratedDataSource.getOpenConnections().isEmpty()) {
                StringBuilder builder = new StringBuilder("Still open connections\n");
                for (DecoratedConnection decoratedConnection : decoratedDataSource.getOpenConnections()) {
                    builder.append("Connection with identifier ").append(decoratedConnection.getIdentifier()).append('\n');
                    for (StackTraceElement stackTraceElement : decoratedConnection.getOriginatingStackTrace()) {
                        builder.append('\t').append(stackTraceElement.toString()).append('\n');
                    }
                }
                throw new IllegalStateException(builder.toString());
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
