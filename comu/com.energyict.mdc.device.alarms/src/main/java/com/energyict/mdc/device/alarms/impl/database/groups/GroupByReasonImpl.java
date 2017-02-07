/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database.groups;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

public class GroupByReasonImpl extends DeviceAlarmGroupOperation {

    protected GroupByReasonImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();

        builder.append("SELECT startDT " + GROUP_KEY + ", reason_id " + GROUP_TITLE + ", count(DAL.ID) " + GROUP_COUNT + " FROM ");
        builder.append(" (SELECT TRUNC ( " + getFilter().getTo() + "- 86400*1000*(ROWNUM )) startDT, TRUNC (" + getFilter().getTo() + " - 86400*1000*(ROWNUM -1)) endDT");
        builder.append(" FROM DUAL CONNECT BY ROWNUM <= 32) DT");
        builder.append(" left join DAL_ALARM_ALL DAL on (DAL.CREATETIME >= DT.startDT and DAL.CREATETIME< DT.endDT)");
        builder.append(" left JOIN ISU_ISSUE_ALL ISU ON (DAL.ALARM = ISU.ID)");
        builder.append(getReasonCondition());
        builder.append(" group by startDT, reason_id");
        builder.append(" order by startDT desc");
        return builder;
    }
}
