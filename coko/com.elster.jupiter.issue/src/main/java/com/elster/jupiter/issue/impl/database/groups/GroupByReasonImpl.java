package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.util.sql.SqlBuilder;

@LiteralSql
public class GroupByReasonImpl extends IssuesGroupOperation {

    protected GroupByReasonImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    /**
     SQL example with 'REASON' grouping column:
     <code>
        SELECT key0, title, count0
        FROM
            (SELECT ROWNUM as rnum, intr.*
            FROM
                (SELECT DISTINCT isu.REASON_ID as key0, reason.TRANSLATION as title0, count(isu.REASON_ID) as count0
                 FROM ISU_ISSUE_ALL isu LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID=device.ID JOIN ISU_REASON reason ON isu.REASON_ID = reason."KEY"
                 WHERE 1=1 GROUP BY (isu.REASON_ID, reason.TRANSLATION)
                 ORDER BY count0 DESC, title ASC
            ) intr
            WHERE ROWNUM <= 5
        ) ext
        WHERE  ext.rnum >= 0;
     </code>
     */
    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_KEY + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT DISTINCT isu.REASON_ID as " + GROUP_KEY + ", reason.TRANSLATION as " + GROUP_TITLE + ", count(isu.REASON_ID) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID = device.ID JOIN " + TableSpecs.ISU_REASON.name());
        builder.append(" reason ON isu.REASON_ID = reason.\"KEY\" WHERE 1=1 ");
        builder.append(getIssueTypeCondition());
        builder.append(getStatusCondition());
        builder.append(getMeterCondition());
        builder.append(getAssigneeCondition());
        if (getFilter().getGroupKey() != null) {
            builder.append(" AND reason.\"KEY\" = '" + getFilter().getGroupKey() + "'");
        }
        builder.append(" GROUP BY (isu.REASON_ID, reason.TRANSLATION)");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getFilter().isAscOrder() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ? ) ext WHERE ext.rnum >= ? ");
        return builder;



/*        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_KEY + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT DISTINCT r.\"KEY\" as " + GROUP_KEY + ", r.TRANSLATION as " + GROUP_TITLE + ", count(isu.REASON_ID) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu, MTR_ENDDEVICE d, " + TableSpecs.ISU_REASON.name());
        builder.append(" r WHERE isu.REASON_ID = r.\"KEY\" AND isu.DEVICE_ID = d.ID");
        builder.append(getIssueTypeCondition());
        builder.append(getStatusCondition());
        builder.append(getMeterCondition());
        builder.append(getAssigneeCondition());
        if (getFilter().getGroupKey() != null) {
            builder.append(" AND r.\"KEY\" = '" + getFilter().getGroupKey() + "'");
        }
        builder.append(" GROUP BY (r.\"KEY\", r.TRANSLATION)");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getFilter().isAscOrder() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ? ) ext WHERE ext.rnum >= ? ");
        return builder;*/
    }

/*    @Override
    protected String getTableName() {
        if (getFilter().getSourceClass().equals(Issue.class)) {
            return TableSpecs.ISU_ISSUE_ALL.name();
        }
        if (getFilter().getSourceClass().equals(OpenIssue.class)) {
            return TableSpecs.ISU_ISSUE_OPEN.name();
        }
        if (getFilter().getSourceClass().equals(HistoricalIssue.class)) {
            return TableSpecs.ISU_ISSUE_HISTORY.name();
        }
    }*/

/*    private String getIssueTypeCondition(){
        if (getFilter().getIssueTypes() != null){
            StringBuilder builder = new StringBuilder();
            for (String issueType : getFilter().getIssueTypes()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                builder.append("reason." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE).append(" = '").append(issueType).append("'");
            }
            if (builder.length() != 0){
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }


*//*            SqlBuilder builder = new SqlBuilder(" AND r." + DatabaseConst.ISSUE_REASON_COLUMN_TYPE);
            builder.append(" = '" + getFilter().getIssueType() + "'");
            return builder.toString();*//*
        }
        return  "";
    }
    private String getStatusCondition(){
        if (getFilter().getStatuses() != null) {
            StringBuilder builder = new StringBuilder();
            for (String status : getFilter().getStatuses()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                builder.append("isu." + DatabaseConst.ISSUE_COLUMN_STATUS_ID).append(" = '").append(status).append("'");
            }
            if (builder.length() != 0){
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }
        return "";
    }

    private String getMeterCondition() {
        if (getFilter().getMeterMrid() != null){
            StringBuilder builder = new StringBuilder();
            builder.append("device.MRID = '").append(getFilter().getMeterMrid()).append("'");
            builder.insert(0, " AND (").append(") ");
            return builder.toString();
        }
        return "";
    }

    private String getAssigneeCondition(){
        if (getFilter().getAssignees() != null) {
            StringBuilder builder = new StringBuilder();
            for (IssueGroupFilter.AssigneeDetails assigneeDetails : getFilter().getAssignees()) {
                if (builder.length() != 0){
                    builder.append(" OR ");
                }
                //builder.append("isu." + DatabaseConst.ISSUE_COLUMN_STATUS_ID).append(" = '").append(status).append("'");
                AssigneeType type = AssigneeType.fromString(assigneeDetails.getAssigneeType());
                if (type != null) {
                    builder.append(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE + " = " + type.ordinal());
                    builder.append(" AND isu.");
                    builder.append(type.getColumnName());
                    builder.append(" = " + assigneeDetails.getAssigneeId());
                }
            }
            if (builder.length() != 0){
                builder.insert(0, " AND (").append(") ");
                return builder.toString();
            }
        }



*//*        AssigneeType type = AssigneeType.fromString(getFilter().getAssigneeType());
        if (type != null){
            SqlBuilder builder = new SqlBuilder(" AND isu.");
            builder.append(DatabaseConst.ISSUE_COLUMN_ASSIGNEE_TYPE + " = " + type.ordinal());
            builder.append(" AND isu.");
            builder.append(type.getColumnName());
            builder.append(" = " + getFilter().getAssigneeId());
            return builder.toString();
        }*//*
        return "";
    }*/
}
