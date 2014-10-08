package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.records.IssueGroupImpl;
import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
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

public abstract class IssuesGroupOperation {
    protected static final Logger LOG = Logger.getLogger(IssuesGroupOperation.class.getName());

    protected static final String GROUP_KEY = "key0";
    protected static final String GROUP_TITLE = "title0";
    protected static final String GROUP_COUNT = "count0";

    private final DataModel dataModel;
    private IssueGroupFilter filter;

    public static IssuesGroupOperation init(IssueGroupFilter builder, DataModel dataModel) {
        if (dataModel == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Data model can't be null");
        }
        if (builder == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Issue group can't be null");
        }
        IssueGroupBy groupColumn = IssueGroupBy.fromString(builder.getGroupBy());
        if (groupColumn == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Grouping column can't be null");
        }
        IssuesGroupOperation operation = groupColumn.getOperationImplementer(dataModel);
        operation.setFilter(builder);
        return operation;
    }

    protected IssuesGroupOperation(DataModel dataModel){
        this.dataModel = dataModel;
    }

    protected IssueGroupFilter getFilter() {
        return filter;
    }

    private void setFilter(IssueGroupFilter filter) {
        this.filter = filter;
    }

    public List<IssueGroup> execute(){
        List<IssueGroup> groups = new LinkedList<>();
        SqlBuilder sql = buildSQL();
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement groupingStatement = buildStatement(conn, sql);
            try (ResultSet rs = groupingStatement.executeQuery()) {
                while (rs.next()) {
                    groups.add(new IssueGroupImpl(rs.getObject(GROUP_KEY), rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT)));
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
        statement.setLong(1, getFilter().getTo());
        statement.setLong(2, getFilter().getFrom());
        return statement;
    }

    protected abstract String getTableName();

    protected DataModel getDataModel() {
        return dataModel;
    }
}