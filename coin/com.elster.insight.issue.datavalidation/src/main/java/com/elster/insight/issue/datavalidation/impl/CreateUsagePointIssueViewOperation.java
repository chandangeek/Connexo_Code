/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class CreateUsagePointIssueViewOperation {
    protected static final Logger LOG = Logger.getLogger(CreateUsagePointIssueViewOperation.class.getName());

    private final DataModel dataModel;

    public CreateUsagePointIssueViewOperation(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void execute() {
        try (Connection conn = dataModel.getConnection(true)) {
            PreparedStatement statement = buildStatement(conn, buildCreateSQL());
            statement.execute();
        } catch (SQLException sqlEx) {
            LOG.log(Level.SEVERE, "Unable to create view for all issues", sqlEx);
        }
    }

    protected SqlBuilder buildCreateSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE OR REPLACE VIEW " + TableSpecs.IUV_ISSUE_ALL + " AS ");
        builder.append("select * from " + TableSpecs.IUV_ISSUE_OPEN.name() + " union select * from " + TableSpecs.IUV_ISSUE_HISTORY.name());
        return builder;
    }

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException {
        return sql.prepare(connection);
    }
}
