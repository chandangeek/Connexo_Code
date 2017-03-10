package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;


public class GroupByWorkGroupAssigneeImpl extends IssuesGroupOperation {

    protected GroupByWorkGroupAssigneeImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_KEY + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT NVL(isu.ASSIGNEE_WORKGROUP_ID, -1) as " + GROUP_KEY + ", NVL(workgroup.NAME, \'" + DatabaseConst.UNASSIGNED + "\') as " + GROUP_TITLE + ", count(NVL(isu.ASSIGNEE_WORKGROUP_ID, -1)) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu ");
        builder.append(" LEFT JOIN DAL_ALARM_OPEN dal ON isu." + getIssueIdColumnName(getTableName()) + " = dal.ID ");
        builder.append(" LEFT JOIN DAL_ALARM_HISTORY dalH ON isu." + getIssueIdColumnName(getTableName()) + " = dalH.ID ");
        builder.append(" LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID = device.ID JOIN " + TableSpecs.ISU_REASON.name());
        builder.append(" reason ON isu.REASON_ID = reason.\"KEY\" JOIN " + TableSpecs.ISU_STATUS.name() + " status ON isu.STATUS = status.\"KEY\"");
        builder.append(" LEFT JOIN USR_WORKGROUP workgroup ON isu.ASSIGNEE_WORKGROUP_ID=workgroup.ID WHERE 1=1 ");
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
        builder.append(" GROUP BY (NVL(isu.ASSIGNEE_WORKGROUP_ID, -1), NVL(workgroup.NAME, \'" + DatabaseConst.UNASSIGNED + "\'))");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getFilter().isAscOrder() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ");
        builder.addLong(getFilter().getTo());
        builder.append(" ) ext WHERE ext.rnum >= ");
        builder.addLong(getFilter().getFrom());
        return builder;
    }
}