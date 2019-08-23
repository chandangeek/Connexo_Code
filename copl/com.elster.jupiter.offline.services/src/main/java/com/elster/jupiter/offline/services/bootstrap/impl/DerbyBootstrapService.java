/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.offline.services.bootstrap.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.bootstrap.DataSourceSetupException;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Component(name = "com.elster.jupiter.bootstrap.offline", immediate = true)
class DerbyBootstrapService implements BootstrapService {

    private static final String USER = "ofline";
    private static final String PASSWORD = "offline";
    private static final String DATABASE_NAME_BASE = "offline.db";
    private Connection connection;
    private EmbeddedDataSource dataSource;

    @Override
    public DataSource createDataSource() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName(DATABASE_NAME_BASE);
        dataSource.setUser(USER);
        dataSource.setPassword(PASSWORD);
        dataSource.setCreateDatabase("create");
        try (Connection connection = dataSource.getConnection()){
//            this.executeStatement(connection, "create alias if not exists regexp_like as $$ boolean regexpLike(String s, String p, String ignore) { return s.matches(p); } $$;");
            this.executeStatement(connection, "create domain if not exists SDO_GEOMETRY as VARCHAR(255)");
            this.executeStatement(connection, "create schema if not exists MDSYS AUTHORIZATION SA");
            this.executeStatement(connection, "create table if not exists MDSYS.USER_SDO_GEOM_METADATA(TABLE_NAME VARCHAR2(32),COLUMN_NAME VARCHAR2(1024),DIMINFO VARCHAR2(1024),SRID INT)");
            this.executeStatement(connection, "create alias if not exists MDSYS.SDO_DIM_ARRAY as $$ String sdoDimArray(String element1, String element2, String element3) { return element1+\" \"+element2+\" \"+element3; } $$;");
            this.executeStatement(connection, "create alias if not exists SDO_DIM_ELEMENT as $$ String sdoDimElement(String coordType, int dimX, int dimY, int dimZ) { return coordType +\" \"+ dimX +\" \"+ dimY +\" \"+ dimZ; } $$;");
        } catch (SQLException e) {
            throw new DataSourceSetupException(e);
        }
        return dataSource;
    }

    private void executeStatement(Connection connection, String statement) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
            preparedStatement.execute();
        }
    }

    @Deactivate
    public void deactivate() {
    	if (dataSource == null) {
    		return;
    	}
        try {
            if (!dataSource.getConnection().isClosed()) {
                throw new IllegalStateException("Still open connections\n");
            }
        } catch(SQLException ex){
            throw new IllegalStateException(ex);
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
