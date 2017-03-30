/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.LocationTemplate;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@LiteralSql
public final class CreateLocationMemberTableOperation {

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
            locationTemplate.getTemplateMembers().stream().filter(templateMember -> !"locale".equalsIgnoreCase(templateMember.getName()))
                    .forEach(column -> {
                        if (column.getRanking() < locationTemplate.getTemplateMembers().size() / 2) {
                            try (
                                    PreparedStatement setIndexesStatement = buildStatement(conn, setIndexesSQL(column.getName()));
                                    PreparedStatement setIndexesForVirtualColumnsStatement = buildStatement(conn, setIndexesSQL("upper" + column
                                            .getName()));
                            ) {
                                setIndexesStatement.execute();
                                setIndexesForVirtualColumnsStatement.execute();
                            } catch (SQLException e) {
                                throw new UnderlyingSQLFailedException(e);
                            }
                        }
                    });
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }


    private SqlBuilder setIndexesSQL(String columnName) {
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE INDEX MTR_IDX_"
                + columnName.toUpperCase().substring(0, columnName.length() > 20 ? 20 : columnName.length() - 1)
                + " ON "
                + TableSpecs.MTR_LOCATIONMEMBER.name()
                + "("
                + columnName.toUpperCase()
                + ")"
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

