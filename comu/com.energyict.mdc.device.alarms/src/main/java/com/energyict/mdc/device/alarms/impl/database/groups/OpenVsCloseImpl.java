/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database.groups;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;

public class OpenVsCloseImpl extends DeviceAlarmGroupOperation {

    protected OpenVsCloseImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    public SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();

        builder.append("SELECT startDT " + GROUP_KEY + ", alarmStatus " + GROUP_TITLE + ", count(DAL.ID) " + GROUP_COUNT + " FROM ");
        builder.append(" (SELECT TRUNC ( " + getFilter().getTo() + "- 86400*1000*(ROWNUM )) startDT, TRUNC (" + getFilter().getTo() + " - 86400*1000*(ROWNUM -1)) endDT");
        builder.append(" FROM DUAL CONNECT BY ROWNUM <= 32) DT");
        builder.append(" left join (");
        builder.append(" select 'open' alarmStatus, DAL_ALARM_OPEN.CREATETIME, DAL_ALARM_OPEN.ID from DAL_ALARM_OPEN");
        builder.append(" JOIN ISU_ISSUE_ALL ISU ON DAL_ALARM_OPEN.ID = ISU.ID ");
        builder.append(getReasonCondition());
        builder.append(" UNION");
        builder.append(" select 'closed' alarmStatus, DAL_ALARM_HISTORY.CREATETIME, DAL_ALARM_HISTORY.ID from DAL_ALARM_HISTORY");
        builder.append(" JOIN ISU_ISSUE_ALL ISU ON DAL_ALARM_HISTORY.ID = ISU.ID ");
        builder.append(getReasonCondition());
        builder.append(" ) DAL on (DAL.CREATETIME >= DT.startDT and DAL.CREATETIME< DT.endDT)");
        builder.append(" group by startDT, alarmStatus");
        builder.append(" order by startDT desc");
        return builder;
    }
}
