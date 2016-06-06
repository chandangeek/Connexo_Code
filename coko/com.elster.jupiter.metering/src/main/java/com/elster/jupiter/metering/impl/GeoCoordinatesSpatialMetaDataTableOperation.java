package com.elster.jupiter.metering.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@LiteralSql
public final class GeoCoordinatesSpatialMetaDataTableOperation {

    private final DataModel dataModel;

    @Inject
    public GeoCoordinatesSpatialMetaDataTableOperation(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void execute() {
        try (Connection conn = dataModel.getConnection(false);
             PreparedStatement dropOldMetaData = buildStatement(conn, dropSpatialMetaDataSql());
             PreparedStatement setMetData = buildStatement(conn, setSpatialMetaDataSql())) {
            dropOldMetaData.execute();
            setMetData.execute();
        } catch (SQLException sqlEx) {
            throw new UnderlyingSQLFailedException(sqlEx);
        }
    }


    private SqlBuilder setSpatialMetaDataSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("INSERT INTO mdsys.user_sdo_geom_metadata VALUES ('"
                + TableSpecs.MTR_GEOCOORDINATES.name()
                + "', '"
                + "GEOCOORDINATES"
                + "', "
                + "MDSYS.SDO_DIM_ARRAY ("
                + "SDO_DIM_ELEMENT ('LONGITUDE',-180,180,1),"
                + "SDO_DIM_ELEMENT ('LATITUDE',-90,90,1)),8307)"
        );
        return builder;
    }


    private SqlBuilder dropSpatialMetaDataSql() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("DELETE FROM mdsys.user_sdo_geom_metadata where table_name = '"
                + TableSpecs.MTR_GEOCOORDINATES.name()
                + "' and column_name = '"
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
