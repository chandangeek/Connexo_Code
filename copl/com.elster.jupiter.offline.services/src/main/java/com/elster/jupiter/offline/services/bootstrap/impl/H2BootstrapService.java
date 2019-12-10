/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;

import org.h2.jdbcx.JdbcDataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component(name = "com.elster.jupiter.bootstrap.offline", service = BootstrapService.class,
        property = {"osgi.command.scope=orm", "osgi.command.function=dbConnection"})
public class H2BootstrapService implements BootstrapService {

    private static final String JDBC_URL = "jdbc:h2:./data/h2Offline;MVCC=TRUE;MODE=ORACLE;lock_timeout=5000";
    private static final String USER = "OFFLINE";
    private static final String PASSWORD = "offline";

    private Connection connection;
    private H2DecoratedDataSource decoratedDataSource;

    @Override
    public DataSource createDataSource() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL(JDBC_URL);
        source.setUser(USER);
        source.setPassword(PASSWORD);
        try {
            connection = source.getConnection();
        } catch (SQLException ex) {
            throw new DataSourceSetupException(ex);
        }
        decoratedDataSource = new H2DecoratedDataSource(source);
        try (Connection connection = decoratedDataSource.getConnection()){
            this.executeStatement(connection, "create domain if not exists SDO_GEOMETRY as VARCHAR(255)");
            this.executeStatement(connection, "create schema if not exists MDSYS AUTHORIZATION OFFLINE");
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
                for (H2DecoratedConnection decoratedConnection : decoratedDataSource.getOpenConnections()) {
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
