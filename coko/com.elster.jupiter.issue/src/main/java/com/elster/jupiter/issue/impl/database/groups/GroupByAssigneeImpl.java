/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.DatabaseConst;
import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;


public class GroupByAssigneeImpl extends IssuesGroupOperation {

    protected GroupByAssigneeImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    /**
     SQL example with 'ASSIGNEE' grouping column:
     <code>
     SELECT key0, title, count0
     FROM
     (SELECT ROWNUM as rnum, intr.*
     FROM
     (SELECT DISTINCT isu.ASSIGNEE_USER_ID as key0, user.AUTHNAME as title0, count(isu.ASSIGNEE_USER_ID) as count0
     FROM ISU_ISSUE_ALL isu LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID = device.ID
     JOIN ISU_STATUS status ON isu.STATUS = status."KEY"
     JOIN ISU_REASON reason ON isu.REASON_ID=reason."KEY"
     LEFT JOIN USR_USER user ON isu.ASSIGNEE_USER_ID=user.ID
     WHERE 1=1 GROUP BY (isu.ASSIGNEE_USER_ID, user.AUTHNAME)
     ORDER BY count0 DESC, title ASC
     ) intr
     WHERE ROWNUM <= 5
     ) ext
     WHERE  ext.rnum >= 0;
     </code>
     The only assignee type supported for now is USER.
     */

    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();
        builder.append("SELECT " + GROUP_KEY + ", " + GROUP_TITLE + ", " + GROUP_COUNT + " FROM " + "(SELECT ROWNUM as rnum, intr.*");
        builder.append(" FROM (SELECT NVL(isu.ASSIGNEE_USER_ID, -1) as " + GROUP_KEY + ", NVL(usr.AUTHNAME, \'" + DatabaseConst.UNASSIGNED + "\') as " + GROUP_TITLE + ", count(NVL(isu.ASSIGNEE_USER_ID, -1)) as " + GROUP_COUNT);
        builder.append(" FROM " + getTableName() + " isu LEFT JOIN MTR_ENDDEVICE device ON isu.DEVICE_ID = device.ID JOIN " + TableSpecs.ISU_REASON.name());
        builder.append(" reason ON isu.REASON_ID = reason.\"KEY\" JOIN " + TableSpecs.ISU_STATUS.name() + " status ON isu.STATUS = status.\"KEY\"");
        builder.append(" LEFT JOIN USR_USER usr ON isu.ASSIGNEE_USER_ID=usr.ID WHERE 1=1 ");
        builder.append(getIssueTypeCondition());
        builder.append(getStatusCondition());
        builder.append(getMeterCondition());
        builder.append(getUserAssigneeCondition());
        builder.append(getWorkGroupCondition());
        builder.append(getDueDateCondition());
        if (getFilter().getGroupKey() != null) {
            builder.append(" AND reason.\"KEY\" = '" + getFilter().getGroupKey() + "'");
        }
        builder.append(" GROUP BY (NVL(isu.ASSIGNEE_USER_ID, -1), NVL(usr.AUTHNAME, \'" + DatabaseConst.UNASSIGNED + "\'))");
        builder.append(" ORDER BY " + GROUP_COUNT + " " + (getFilter().isAscOrder() ? "ASC" : "DESC") + ", " + GROUP_TITLE + " ASC ) intr");
        builder.append(" WHERE ROWNUM <= ");
        builder.addLong(getFilter().getTo());
        builder.append(" ) ext WHERE ext.rnum >= ");
        builder.addLong(getFilter().getFrom());
        return builder;
    }
}
