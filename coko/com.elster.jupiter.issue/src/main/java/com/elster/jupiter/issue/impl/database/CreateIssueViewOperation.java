package com.elster.jupiter.issue.impl.database;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
public class CreateIssueViewOperation {
    protected static final Logger LOG = Logger.getLogger(CreateIssueViewOperation.class.getName());

    private final DataModel dataModel;

    public static CreateIssueViewOperation init(DataModel dataModel) {
        if (dataModel == null){
            throw new IllegalArgumentException("[ CreateIssueViewOperation ] Data model can't be null");
        }
        return new CreateIssueViewOperation(dataModel);
    }

    protected CreateIssueViewOperation(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public void execute(){
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement statement = buildStatement(conn, buildCreateSQL());
            statement.execute();
        } catch (SQLException sqlEx){
            LOG.log(Level.SEVERE, "Unable to create view for all issues", sqlEx);
        }
    }

    protected SqlBuilder buildCreateSQL(){
        SqlBuilder builder = new SqlBuilder();
        builder.append("CREATE OR REPLACE VIEW " + DatabaseConst.ALL_ISSUES_VIEW_NAME + " AS ");
        builder.append("select * from " + TableSpecs.ISU_ISSUE.name() + " union select * from " + TableSpecs.ISU_ISSUEHISTORY.name());
        return builder;
    }

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException{
        if (connection == null){
            throw new IllegalArgumentException("[ CreateIssueViewOperation ] Connection can't be null");
        }
        return sql.prepare(connection);
    }
}
