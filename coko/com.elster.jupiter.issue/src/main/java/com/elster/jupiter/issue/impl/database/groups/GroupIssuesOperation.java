package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.share.entity.GroupByReasonEntity;
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

    protected final DataModel dataModel;

    protected long id;
    protected long to;
    protected long from;
    protected boolean isAsc = true;

    public static GroupIssuesOperation init(IssueGroupColumns groupBy, DataModel dataModel) {
        if (dataModel == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Data model can't be null");
        }
        if (groupBy == null){
            throw new IllegalArgumentException("[ GroupIssuesOperation ] Column is required for issue grouping operation");
        }
        return groupBy.getOperationImplementer(dataModel);
    }

    protected GroupIssuesOperation(DataModel dataModel){
        this.dataModel = dataModel;
    }

    public GroupIssuesOperation setId(long id){
        this.id = id;
        return this;
    }

    protected long getId() {
        return this.id;
    }

    public GroupIssuesOperation setTo(long to){
        this.to = to;
        return this;
    }

    public GroupIssuesOperation setFrom(long from){
        this.from = from;
        return this;
    }

    public GroupIssuesOperation setOrderDirection(boolean isAsc) {
        this.isAsc = isAsc;
        return this;
    }

    public List<GroupByReasonEntity> execute(){
        List<GroupByReasonEntity> groups = new LinkedList<>();
        SqlBuilder sql = buildSQL();
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement groupingStatement = buildStatement(conn, sql);
            ResultSet rs = groupingStatement.executeQuery();
            while (rs.next()) {
                groups.add(new GroupByReasonEntity(rs.getLong(GROUP_ID), rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT)));
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
        if (this.id != 0) {
            statement.setLong(1, this.id);
            statement.setLong(2, this.to);
            statement.setLong(3, this.from);
        }
        else {
            statement.setLong(1, this.to);
            statement.setLong(2, this.from);
        }
        return statement;
    }

}