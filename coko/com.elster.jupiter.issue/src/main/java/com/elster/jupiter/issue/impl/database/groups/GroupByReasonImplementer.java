package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.impl.records.assignee.types.AssigneeTypes;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

@LiteralSql
public class GroupByReasonImplementer extends GroupIssuesOperation {

    protected GroupByReasonImplementer(DataModel dataModel) {
        super(dataModel);
    }

    /**
     SQL example with 'REASON' grouping column:
     <code>
        SELECT idcol, col, num
        FROM
            (SELECT ROWNUM as rnum, intr.*
            FROM
                (SELECT DISTINCT r.ID as idcol, r.REASON_NAME as col, count(isu.REASON_ID) as num
                FROM ISU_ISSUE isu, ISU_REASON r
                WHERE isu.REASON_ID = r.ID AND r.ID = 3
                GROUP BY (r.ID, r.REASON_NAME)
                ORDER BY num DESC, col ASC
            ) intr
            WHERE ROWNUM <= 5
        ) ext
        WHERE  ext.rnum >= 0;
     </code>
     */
    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_ID + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT DISTINCT r.ID as " + GROUP_ID + ", r.REASON_NAME as " + GROUP_TITLE + ", count(isu.REASON_ID) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu, " + TableSpecs.ISU_REASON.name());
        builder.append(" r WHERE isu.REASON_ID = r.ID");
        builder.append(getIssueTypeCondition());
        builder.append(getStatusCondition());
        builder.append(getMeterCondition());
        builder.append(getAssigneeCondition());
        if (getBuilder().getId() != 0) {
            builder.append(" AND r.ID = ?");
        }
        builder.append(" GROUP BY (r.ID, r.REASON_NAME)");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getBuilder().isAsc() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ? ) ext WHERE ext.rnum >= ? ");
        return builder;
    }

    private String getIssueTypeCondition(){
        if (getBuilder().getIssueType() != null){
            SqlBuilder builder = new SqlBuilder(" AND r." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE);
            builder.append(" = '" + getBuilder().getIssueType() + "'");
            return builder.toString();
        }
        return  "";
    }
    private String getStatusCondition(){
        if (getBuilder().getStatuses() != null) {
            StringBuilder builder = new StringBuilder();
            for (String status : getBuilder().getStatuses()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_STATUS_ID).append(" = ").append(status);
            }
            if (builder.length() != 0){
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }
        return "";
    }

    private String getMeterCondition() {
        if (getBuilder().getMeterId() != 0){
            StringBuilder builder = new StringBuilder();
            builder.append("isu." + DatabaseConst.ISSUE_COLUMN_DEVICE_ID).append(" = ").append(getBuilder().getMeterId());
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    private String getAssigneeCondition(){
        AssigneeTypes type = AssigneeTypes.fromString(getBuilder().getAssigneeType());
        if (type != null){
            SqlBuilder builder = new SqlBuilder(" AND isu.");
            builder.append(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE + " = " + type.ordinal());
            builder.append(" AND isu.");
            builder.append(type.getColumnName());
            builder.append(" = " + getBuilder().getAssigneeId());
            return builder.toString();
        }
        return "";
    }
}
