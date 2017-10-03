package com.energyict.mdc.device.alarms.impl.database.groups;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;


public class GroupByPriorityImpl extends DeviceAlarmGroupOperation {

    protected GroupByPriorityImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel, thesaurus);
    }

    @Override
    protected SqlBuilder buildSQL() {
        SqlBuilder builder = new SqlBuilder();

        builder.append("SELECT startDT " + GROUP_KEY + ", alarmPriority " + GROUP_TITLE + ", count(DAL.ID) " + GROUP_COUNT + " FROM ");
        builder.append(" (SELECT TRUNC ( " + getFilter().getTo() + "- 86400*1000*(ROWNUM )) startDT, TRUNC (" + getFilter()
                .getTo() + " - 86400*1000*(ROWNUM -1)) endDT");
        builder.append(" FROM DUAL CONNECT BY ROWNUM <= 32) DT");
        builder.append(" left join (");
        builder.append(" select case ");
        builder.append(" WHEN PRIORITYTOTAL <=20 THEN 'veryLow' ");
        builder.append(" WHEN PRIORITYTOTAL <=40 THEN 'low' ");
        builder.append(" WHEN PRIORITYTOTAL <=60 THEN 'medium' ");
        builder.append(" WHEN PRIORITYTOTAL <=80 THEN 'high' ");
        builder.append(" WHEN PRIORITYTOTAL <=100 THEN 'veryHigh' ");
        builder.append(" END alarmPriority ");
        builder.append(", ISU.CREATEDATETIME CREATETIME, DAL.ID ID from DAL_ALARM_ALL DAL");
        builder.append(" JOIN ISU_ISSUE_ALL ISU ON DAL.ID = ISU.ID ");
        builder.append(getReasonCondition());
        builder.append(" ) DAL on (DAL.CREATETIME >= DT.startDT and DAL.CREATETIME< DT.endDT)");
        builder.append(" group by startDT, alarmPriority");
        builder.append(" order by startDT desc");
        return builder;
    }
}
