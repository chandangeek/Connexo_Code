package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.records.GroupByReasonEntityImpl;
import com.elster.jupiter.issue.share.entity.BaseIssue;
import com.elster.jupiter.issue.share.entity.GroupByReasonEntity;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.service.GroupQueryBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class GroupIssuesOperation{
    protected static final Logger LOG = Logger.getLogger(GroupIssuesOperation.class.getName());

    protected static final String GROUP_ID = "idcol";
    protected static final String GROUP_TITLE = "col";
    protected static final String GROUP_COUNT = "num";

    private final DataModel dataModel;
    private GroupQueryBuilder builder;

    public static GroupIssuesOperation init(GroupQueryBuilder builder, DataModel dataModel) {
        if (dataModel == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Data model can't be null");
        }
        if (builder == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Query builder can't be null");
        }
        IssueGroupColumns groupColumn = IssueGroupColumns.fromString(builder.getGroupColumn());
        if (groupColumn == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Grouping column can't be null");
        }
        GroupIssuesOperation operation = groupColumn.getOperationImplementer(dataModel);
        operation.setBuilder(builder);
        return operation;
    }

    protected GroupIssuesOperation(DataModel dataModel){
        this.dataModel = dataModel;
    }

    protected GroupQueryBuilder getBuilder() {
        return builder;
    }

    private void setBuilder(GroupQueryBuilder builder) {
        this.builder = builder;
    }

    public List<GroupByReasonEntity> execute(){
        List<GroupByReasonEntity> groups = new LinkedList<>();
        SqlBuilder sql = buildSQL();
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement groupingStatement = buildStatement(conn, sql);
            try (ResultSet rs = groupingStatement.executeQuery()) {
                while (rs.next()) {
                    groups.add(new GroupByReasonEntityImpl(rs.getLong(GROUP_ID), rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT)));
                }
            }
        } catch (SQLException sqlEx){
            LOG.log(Level.SEVERE, "Unable to retrieve grouped list from database", sqlEx);
        }
        return groups;
    }

    protected abstract SqlBuilder buildSQL();

    protected PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException{
        if (connection == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Connection can't be null");
        }
        PreparedStatement statement = sql.prepare(connection);
        if (getBuilder().getId() != 0) {
            statement.setLong(1, getBuilder().getId());
            statement.setLong(2, getBuilder().getTo());
            statement.setLong(3, getBuilder().getFrom());
        } else {
            statement.setLong(1, getBuilder().getTo());
            statement.setLong(2, getBuilder().getFrom());
        }
        return statement;
    }

    protected String getTableName(){
        Class<?> apiClass = getBuilder().getSourceClass();
        if (BaseIssue.class.equals(apiClass)){
            return DatabaseConst.ALL_ISSUES_VIEW_NAME;
        } else if (HistoricalIssue.class.equals(apiClass)){
            return TableSpecs.ISU_ISSUEHISTORY.name();
        }
        return TableSpecs.ISU_ISSUE.name();
    }

    protected DataModel getDataModel() {
        return dataModel;
    }
}