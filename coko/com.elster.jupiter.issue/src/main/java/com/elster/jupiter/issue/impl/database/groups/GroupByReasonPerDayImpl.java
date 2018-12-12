/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

public class GroupByReasonPerDayImpl extends IssuesGroupOperation {

    protected GroupByReasonPerDayImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();

        builder.append("SELECT startDT " + GROUP_KEY + ", reason_id " + GROUP_TITLE + ", count(ISU.ID) " + GROUP_COUNT + " FROM ");
        builder.append(" (SELECT TRUNC ( " + getFilter().getTo() + "- 86400*1000*(ROWNUM )) startDT, TRUNC (" + getFilter().getTo() + " - 86400*1000*(ROWNUM -1)) endDT");
        builder.append(" FROM DUAL CONNECT BY ROWNUM <= 32) DT");
        builder.append(" LEFT JOIN (");
        builder.append(" SELECT ISU.CREATEDATETIME, ISU.ID, reason.\"KEY\" reason_id FROM ISU_ISSUE_ALL ISU ");
        builder.append(" LEFT JOIN ISU_REASON reason on isu.reason_id = reason.key ");
        builder.append(" WHERE 1=1 ");
        builder.append(getReasonCondition());
        builder.append(getIssueTypeCondition());
        builder.append(" ) ISU ON (ISU.CREATEDATETIME >= DT.startDT and ISU.CREATEDATETIME< DT.endDT)");
        builder.append(" GROUP BY startDT, reason_id");
        builder.append(" ORDER BY startDT DESC");
        return builder;
    }
}
