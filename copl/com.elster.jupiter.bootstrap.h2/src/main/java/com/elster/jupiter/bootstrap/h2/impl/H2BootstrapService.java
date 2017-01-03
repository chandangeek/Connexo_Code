package com.elster.jupiter.bootstrap.h2.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;

import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;

@Component(name = "com.elster.jupiter.bootstrap.h2", immediate = true)
class H2BootstrapService implements BootstrapService {

    private static final String JDBC_URL_PATTERN = "jdbc:h2:mem:{0};MVCC=TRUE;lock_timeout={1}";
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static final String DATABASE_NAME_BASE = "DB";

    private Connection connection;
    private DecoratedDataSource decoratedDataSource;

    @Override
    public DataSource createDataSource() {
        String jdbcUrl = MessageFormat.format(JDBC_URL_PATTERN, DATABASE_NAME_BASE, "5000");
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
//            this.executeStatement(connection, "create alias if not exists regexp_like as $$ boolean regexpLike(String s, String p, String ignore) { return s.matches(p); } $$;");
            this.executeStatement(connection, "create domain if not exists SDO_GEOMETRY as VARCHAR(255)");
            this.executeStatement(connection, "create schema if not exists MDSYS AUTHORIZATION SA");
            this.executeStatement(connection, "create table if not exists MDSYS.USER_SDO_GEOM_METADATA(TABLE_NAME VARCHAR2(32),COLUMN_NAME VARCHAR2(1024),DIMINFO VARCHAR2(1024),SRID INT)");
            this.executeStatement(connection, "create alias if not exists MDSYS.SDO_DIM_ARRAY as $$ String sdoDimArray(String element1, String element2, String element3) { return element1+\" \"+element2+\" \"+element3; } $$;");
            this.executeStatement(connection, "create alias if not exists SDO_DIM_ELEMENT as $$ String sdoDimElement(String coordType, int dimX, int dimY, int dimZ) { return coordType +\" \"+ dimX +\" \"+ dimY +\" \"+ dimZ; } $$;");
        } catch (SQLException e) {
            throw new DataSourceSetupException(e);
        }
        return decoratedDataSource;
    }

    private void executeStatement(Connection connection, String statement) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.execute();
        }
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
