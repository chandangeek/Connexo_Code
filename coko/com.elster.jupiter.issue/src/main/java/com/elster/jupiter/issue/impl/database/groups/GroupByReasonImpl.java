package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.TableSpecs;
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
     * SQL example with 'REASON' grouping column:
     * <code>
     * SELECT key0, title, count0
     * FROM
     * (SELECT ROWNUM as rnum, intr.*
     * FROM
     * (SELECT DISTINCT isu.REASON_ID as key0, reason.TRANSLATION as title0, count(isu.REASON_ID) as count0
     * FROM ISU_ISSUE_ALL isu LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID=device.ID JOIN ISU_REASON reason ON isu.REASON_ID = reason."KEY"
     * WHERE 1=1 GROUP BY (isu.REASON_ID, reason.TRANSLATION)
     * ORDER BY count0 DESC, title ASC
     * ) intr
     * WHERE ROWNUM <= 5
     * ) ext
     * WHERE  ext.rnum >= 0;
     * </code>
     */
    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_KEY + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT DISTINCT isu.REASON_ID as " + GROUP_KEY + ", reason.TRANSLATION as " + GROUP_TITLE + ", count(isu.REASON_ID) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu LEFT JOIN DAL_ALARM_OPEN dal ON isu." + getIssueIdColumnName(getTableName()) + " = dal.ID LEFT JOIN DAL_ALARM_HISTORY dalH ON isu." + getIssueIdColumnName(getTableName()) + " = dalH.ID LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID = device.ID JOIN " + TableSpecs.ISU_REASON.name());
        builder.append(" reason ON isu.REASON_ID = reason.\"KEY\" WHERE 1=1 ");
        builder.append(getIssueTypeCondition());
        builder.append(getStatusCondition());
        builder.append(getMeterCondition());
        builder.append(getClearedStatuses());
        builder.append(getUserAssigneeCondition());
        builder.append(getWorkGroupCondition());
        builder.append(getDueDateCondition());
        builder.append(getIdCondition());
        if (getFilter().getGroupKey() != null) {
            builder.append(" AND reason.\"KEY\" = '" + getFilter().getGroupKey() + "'");
        }
        builder.append(" GROUP BY (isu.REASON_ID, reason.TRANSLATION)");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getFilter().isAscOrder() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ");
        builder.addLong(getFilter().getTo());
        builder.append(" ) ext WHERE ext.rnum >= ");
        builder.addLong(getFilter().getFrom());
        return builder;
    }
}
