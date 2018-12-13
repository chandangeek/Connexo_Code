/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.database.groups;

import com.elster.jupiter.issue.impl.database.TableSpecs;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

public class OpenVsCloseImpl extends IssuesGroupOperation {

    protected OpenVsCloseImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();

        builder.append("SELECT startDT " + GROUP_KEY + ", issueStatus " + GROUP_TITLE + ", count(ISU.ID) " + GROUP_COUNT + " FROM ");
        builder.append(" (SELECT TRUNC ( " + getFilter().getTo() + "- 86400*1000*(ROWNUM )) startDT, TRUNC (" + getFilter().getTo() + " - 86400*1000*(ROWNUM -1)) endDT");
        builder.append(" FROM DUAL CONNECT BY ROWNUM <= 32) DT");
        builder.append(" left join (");
        builder.append(" select 'open' issueStatus, ISU.CREATEDATETIME CREATETIME, ISU.ID ID from ISU_ISSUE_OPEN ISU");
        builder.append(" LEFT JOIN " + TableSpecs.ISU_REASON.name() + " reason ");
        builder.append(" ON ISU.REASON_ID = reason.\"KEY\" WHERE 1=1 ");
        builder.append(getReasonCondition());
        builder.append(getIssueTypeCondition());
        builder.append(" UNION");
        builder.append(" select 'closed' issueStatus, ISU.CREATEDATETIME CREATETIME, ISU.ISU_HIST_ISSUE_ID ID from ISU_ISSUE_HISTORY ISU");
        builder.append(" LEFT JOIN " + TableSpecs.ISU_REASON.name() + " reason ");
        builder.append(" ON ISU.REASON_ID = reason.\"KEY\" WHERE 1=1 ");
        builder.append(getReasonCondition());
        builder.append(getIssueTypeCondition());
        builder.append(" ) ISU on (ISU.CREATETIME >= DT.startDT and ISU.CREATETIME< DT.endDT)");
        builder.append(" group by startDT, issueStatus");
        builder.append(" order by startDT desc");
        return builder;
    }
}
