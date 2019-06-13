/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

public class GroupByLocationImpl extends IssuesGroupOperation {

    protected GroupByLocationImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }
	 
    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_KEY + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
//        builder.append(" FROM (SELECT NVL(loc.ID, -1) as " + GROUP_KEY + ", NVL(mmbr.LOCALITY, \'" + DatabaseConst.UNASSIGNED + "\') as " + GROUP_TITLE + ", count(NVL(loc.ID, -1)) as " + GROUP_COUNT);
        builder.append(" FROM (SELECT NVL(loc.ID, -1) as " + GROUP_KEY + ", NVL(loc.ID, -1) as " + GROUP_TITLE + ", count(NVL(loc.ID, -1)) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu ");
        builder.append(" LEFT JOIN DAL_ALARM_OPEN dal ON isu." + getIssueIdColumnName(getTableName()) + " = dal.ID ");
        builder.append(" LEFT JOIN DAL_ALARM_HISTORY dalH ON isu." + getIssueIdColumnName(getTableName()) + " = dalH.ID ");
        builder.append(" LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID = device.ID ");
        builder.append(" JOIN " + TableSpecs.ISU_REASON.name() + " reason ON isu.REASON_ID = reason.\"KEY\"");
        builder.append(" JOIN " + TableSpecs.ISU_STATUS.name() + " status ON isu.STATUS = status.\"KEY\"");

        builder.append(" LEFT JOIN MTR_LOCATION loc ON device.LOCATIONID = loc.ID ");
//        builder.append(" LEFT JOIN MTR_LOCATIONMEMBER mmbr ON mmbr.LOCATIONID = loc.ID ");
        builder.append(" WHERE 1=1 ");

        builder.append(getIssueTypeCondition());
        builder.append(getStatusCondition());
        builder.append(getMeterCondition());
        builder.append(getClearedStatuses());
        builder.append(getUserAssigneeCondition());
        builder.append(getWorkGroupCondition());
        builder.append(getDueDateCondition());
        builder.append(getIdCondition());
        builder.append(getMetersCondition());
        if (getFilter().getGroupKey() != null) {
            builder.append(" AND reason.\"KEY\" = '" + getFilter().getGroupKey() + "'");
        }
//        builder.append(" GROUP BY (NVL(loc.ID, -1), NVL(mmbr.LOCALITY, \'" + DatabaseConst.UNASSIGNED + "\'))");
        builder.append(" GROUP BY (NVL(loc.ID, -1), NVL(loc.ID, -1))");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getFilter().isAscOrder() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ");
        builder.addLong(getFilter().getTo());
        builder.append(" ) ext WHERE ext.rnum >= ");
        builder.addLong(getFilter().getFrom());
        return builder;
    }
}
