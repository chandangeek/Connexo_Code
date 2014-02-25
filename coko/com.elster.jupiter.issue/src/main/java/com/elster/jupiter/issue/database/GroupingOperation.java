package com.elster.jupiter.issue.database;

import com.elster.jupiter.issue.impl.IssueGroupColumns;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@LiteralSql
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
        SqlBuilder sql = buildSQL();
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

    private PreparedStatement buildStatement(Connection connection, SqlBuilder sql) throws SQLException{
        PreparedStatement statement = null;
        if (connection == null){
            throw new IllegalArgumentException("[ Grouping Operation ]  Connection can't be null");
        }
        statement = sql.prepare(connection);
        statement.setLong(1, this.start + this.limit);
        statement.setLong(2, this.start);
        return statement;
    }
    /**
    SQL example with 'REASON' grouping column:
     <code>
        SELECT col, num
        FROM
            (SELECT ROWNUM as rnum, r.REASON_NAME as col, intr.num
             FROM
                (SELECT DISTINCT isu.REASON_ID as reasonId, count(isu.REASON_ID) as num
                 FROM ISU_ISSUE isu
                 GROUP BY (isu.REASON_ID)
                 ORDER BY num DESC, reasonId ASC
                ) intr
             LEFT JOIN ISU_REASON r ON intr.reasonId = r.ID
             WHERE ROWNUM <= 10
            ) ext
        WHERE ext.rnum > 0;
     </code>
    */
    private SqlBuilder buildSQL(){
        if(this.groupBy == null){
            throw new IllegalArgumentException("[ Grouping Operation ] Group By column can't be empty");
        }
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, r.");
        builder.append(DatabaseConst.ISSUE_REASON_COLUMN_NAME + " as " + GROUP_TITLE + ", intr." + GROUP_COUNT);
        builder.append(" FROM (SELECT DISTINCT isu.REASON_ID as reasonId, count(isu.REASON_ID) as " + GROUP_COUNT);
        builder.append(" FROM " + TableSpecs.ISU_ISSUE.name() + " isu GROUP BY (isu.REASON_ID)");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (isAsc ? "ASC" : "DESC") + ", reasonId ASC) intr");
        builder.append(" LEFT JOIN " + TableSpecs.ISU_REASON.name() + " r ON intr.reasonId = r.ID");
        builder.append(" WHERE ROWNUM <= ? ) ext WHERE ext.rnum > ? ");
        return builder;
    }
}