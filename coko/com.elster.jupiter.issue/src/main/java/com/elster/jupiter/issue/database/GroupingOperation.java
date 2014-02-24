package com.elster.jupiter.issue.database;

import com.elster.jupiter.issue.impl.IssueGroupColumns;
import com.elster.jupiter.orm.DataModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroupingOperation {
    private static Logger LOG = Logger.getLogger(GroupingOperation.class.getName());
    public static final long DEFAULT_LIMIT = 20l;

    private static final String GROUP_TITLE = "col";
    private static final String GROUP_COUNT = "num";

    private final DataModel dataModel;
    private long limit = DEFAULT_LIMIT;
    private long start = 0;
    private boolean isAsc = true;
    private IssueGroupColumns groupBy;

    public static GroupingOperation init(DataModel dataModel) {
        if (dataModel == null){
            throw new IllegalArgumentException("[ Grouping Operation ]  Data model can't be null");
        }
        return new GroupingOperation(dataModel);
    }

    private GroupingOperation (DataModel dataModel){
        this.dataModel = dataModel;
    }

    public GroupingOperation setLimit(long limit){
        this.limit = limit;
        return this;
    }

    public GroupingOperation setStart(long start){
        this.start = start;
        return this;
    }

    public GroupingOperation setOrderDirection(boolean isAsc){
        this.isAsc = isAsc;
        return this;
    }

    public GroupingOperation setGroupColumn(IssueGroupColumns column){
        this.groupBy = column;
        return this;
    }

    public Map<String, Long> execute(){
        Map<String, Long> groups = new LinkedHashMap<>();
        String sql = buildSQL();
        try (Connection conn = dataModel.getConnection(false)) {
            PreparedStatement groupingStatement =  buildStatement(conn, sql);
            ResultSet rs = groupingStatement.executeQuery();
            while (rs.next()) {
                groups.put(rs.getString(GROUP_TITLE), rs.getLong(GROUP_COUNT));
            }
        } catch (SQLException sqlEx){
            LOG.log(Level.SEVERE, "Unable to retrieve grouped list from database", sqlEx);
        }
        return groups;
    }

    private PreparedStatement buildStatement(Connection connection, String sql) throws SQLException{
        PreparedStatement statement = null;
        if (connection == null){
            throw new IllegalArgumentException("[ Grouping Operation ]  Connection can't be null");
        }
        statement = connection.prepareStatement(sql);
        statement.setLong(1, this.start + this.limit);
        statement.setLong(2, this.start);
        return statement;
    }

    /**
    SQL example with 'REASON' grouping column:
     <code>
        SELECT col, num
        FROM
            (SELECT ROWNUM as rnum, intr.*
             FROM
                (SELECT DISTINCT isu.reason as col, count(isu.reason) as num
                 FROM ISU_ISSUE isu
                 GROUP BY (isu.reason)
                 ORDER BY num DESC, col ASC
                ) intr
             WHERE ROWNUM <= 20
            ) ext
        WHERE ext.rnum > 0;
     </code>
    */
    private String buildSQL(){
        if(this.groupBy == null){
            throw new IllegalArgumentException("[ Grouping Operation ] Group By column can't be empty");
        }
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.* FROM (SELECT DISTINCT ISU.");
        sql.append(this.groupBy.name()).append(" AS " + GROUP_TITLE + ", COUNT(ISU.");
        sql.append(this.groupBy.name()).append(") AS " + GROUP_COUNT + " FROM ").append(TableSpecs.ISU_ISSUE.name()).append(" ISU GROUP BY ISU.");
        sql.append(this.groupBy.name()).append(" ORDER BY " + GROUP_COUNT).append(" ").append(this.isAsc ? "ASC" : "DESC");
        sql.append(", ").append(GROUP_TITLE).append(" ASC");
        sql.append(") intr WHERE ROWNUM <= ?) ext WHERE ext.rnum > ?");
        return sql.toString();
    }
}