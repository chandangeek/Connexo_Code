package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class CreateLocationMemberTableOperation {

    protected static final Logger LOG = Logger.getLogger(CreateLocationMemberTableOperation.class.getName());

    private final DataModel dataModel;
    private final LocationTemplate locationTemplate;

    @Inject
    public CreateLocationMemberTableOperation(DataModel dataModel, LocationTemplate locationTemplate) {
        this.dataModel = dataModel;
        this.locationTemplate = locationTemplate;
    }

    public void execute() {
        try (Connection conn = dataModel.getConnection(false)) {
            locationTemplate.parseTemplate(locationTemplate.getTemplateFields(), locationTemplate.getMandatoryFields());
            locationTemplate.getTemplateMembers().stream().filter(f -> !f.getName().equalsIgnoreCase("locale"))
                    .forEach(column -> {
                        if (column.getRanking() < locationTemplate.getTemplateMembers().size() / 2) {
                            try {
                                buildStatement(conn, setIndexesSQL(column.getName())).execute();
                                buildStatement(conn, setIndexesSQL("upper" + column.getName())).execute();
                            } catch (SQLException sqlEx) {
                                LOG.log(Level.SEVERE, "Unable to create indexes for MTR_LOCATIONMEMBER table", sqlEx);
                            }
                        }
                    });
        } catch (SQLException sqlEx) {
            LOG.log(Level.SEVERE, "Unable to set MTR_LOCATIONMEMBER table", sqlEx);
        }
    }


    protected SqlBuilder setIndexesSQL(String columnName) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE INDEX MTR_IDX_"
                + columnName.toUpperCase().substring(0,columnName.length()>30?30:columnName.length()-1)
                + " ON "
                + TableSpecs.MTR_LOCATIONMEMBER.name()
                + "("
                + columnName.toUpperCase()
                + ")"
        );
        return builder;
    }

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException {
        if (connection == null) {
            throw new IllegalArgumentException("Connection can't be null");
        }
        return sql.prepare(connection);
    }

}

