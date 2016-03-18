package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class CreateLocationMemberTableOperation {

    protected static final Logger LOG = Logger.getLogger(CreateLocationMemberTableOperation.class.getName());

    private final DataModel dataModel;
    private final LocationTemplate locationTemplate;

    public CreateLocationMemberTableOperation(DataModel dataModel, LocationTemplate locationTemplate) {
        this.dataModel = dataModel;
        this.locationTemplate = locationTemplate;
    }

    public void execute() {
        try (Connection conn = dataModel.getConnection(false)) {
            locationTemplate.getMandatoryFieldsNames().stream().forEach(columnName -> {
                try {
                    buildStatement(conn, setNontNullableColumnsSQL(columnName)).execute();
                } catch (SQLException sqlEx) {
                    LOG.log(Level.SEVERE, "Unable to set mandatory fields for MTR_LOCATIONMEMBER table", sqlEx);
                }
            });
            Map<String, Integer> columns = locationTemplate.getRankings();
            columns.remove("locale");
            columns.entrySet().stream().sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey).limit(columns.size()/2).forEach(columnName -> {
                try {
                    buildStatement(conn, setIndexesSQL(columnName)).execute();
                } catch (SQLException sqlEx) {
                    LOG.log(Level.SEVERE, "Unable to create indexes for MTR_LOCATIONMEMBER table", sqlEx);
                }
            });
        } catch (SQLException sqlEx) {
            LOG.log(Level.SEVERE, "Unable to set MTR_LOCATIONMEMBER table", sqlEx);
        }
    }

    protected SqlBuilder setNontNullableColumnsSQL(String columnName) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("ALTER TABLE " + TableSpecs.MTR_LOCATIONMEMBER.name() + " MODIFY ( "
                + columnName.toUpperCase() + " NOT NULL)");
        return builder;
    }

    protected SqlBuilder setIndexesSQL(String columnName) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE INDEX MTR_IDX_"
                + columnName.toUpperCase() + " ON "
                + TableSpecs.MTR_LOCATIONMEMBER.name()
                + " ( " + columnName.toUpperCase() + ", UPPER( " + columnName.toUpperCase() + " )"
                + " )");
        return builder;
    }

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection can't be null");
        }
        return sql.prepare(connection);
    }
}

