/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@LiteralSql
public final class GeoCoordinatesSpatialMetaDataTableOperation {

    private final DataModel dataModel;

    @Inject
    public GeoCoordinatesSpatialMetaDataTableOperation(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void execute() {
        List<PreparedStatement> statements = new ArrayList<>();
        try (Connection conn = dataModel.getConnection(false);
             PreparedStatement dropOldMetaData = buildStatement(conn, dropSpatialMetaDataSql());
             PreparedStatement setMetDataforEndDevice = buildStatement(conn, setSpatialMetaDataSql(TableSpecs.MTR_ENDDEVICE));
             PreparedStatement setMetDataforUsagePoint = buildStatement(conn, setSpatialMetaDataSql(TableSpecs.MTR_USAGEPOINT))) {
            dropOldMetaData.execute();
            setMetDataforEndDevice.execute();
            setMetDataforUsagePoint.execute();
        } catch (SQLException sqlEx) {
            throw new UnderlyingSQLFailedException(sqlEx);
        }
    }


    private SqlBuilder setSpatialMetaDataSql(TableSpecs table) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("INSERT INTO mdsys.user_sdo_geom_metadata VALUES ('"
                + table.name()
                + "', '"
                + "GEOCOORDINATES"
                + "', "
                + "MDSYS.SDO_DIM_ARRAY ("
                + "SDO_DIM_ELEMENT ('LONGITUDE',-180,180,1),"
                + "SDO_DIM_ELEMENT ('LATITUDE',-90,90,1),"
                + "SDO_DIM_ELEMENT ('ELEVATION',-10000,10000,1)"
                + "),8307)"
        );
        return builder;
    }


    private SqlBuilder dropSpatialMetaDataSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("DELETE FROM mdsys.user_sdo_geom_metadata where table_name IN ('"
                + TableSpecs.MTR_ENDDEVICE.name()
                + "', '"
                + TableSpecs.MTR_USAGEPOINT.name()
                + "') and column_name = '"
                + "GEOCOORDINATES'"
        );
        return builder;
    }

    private PreparedStatement buildStatement(Connection connection, SqlBuilder sql) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection can't be null");
        }
        try {
            return sql.prepare(connection);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

}
