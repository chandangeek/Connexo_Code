package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.Never;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;

import java.util.Collections;

public class CrlRequestTaskInfoFactory {

    public CrlRequestTaskInfoFactory() {
    }

    public CrlRequestTaskPropertyInfo asInfo(CrlRequestTaskProperty crlRequestTaskProperty) {
        CrlRequestTaskPropertyInfo info = new CrlRequestTaskPropertyInfo();
        info.recurrentTaskId = crlRequestTaskProperty.getRecurrentTask().getId();
        info.recurrentTaskName = crlRequestTaskProperty.getRecurrentTask().getName();
        info.securityAccessorName = crlRequestTaskProperty.getSecurityAccessor().getKeyAccessorType().getName();
        info.securityAccessorNames = Collections.emptyList();
        info.caName = crlRequestTaskProperty.getCaName();
        ScheduleExpression scheduleExpression = crlRequestTaskProperty.getRecurrentTask().getScheduleExpression();
        if (Never.NEVER.equals(scheduleExpression)) {
            info.schedule = null;
        } else {
            if (scheduleExpression instanceof TemporalExpression) {
                info.schedule = new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression);
            } else {
                info.schedule = PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
            }
        }
        info.nextRun = crlRequestTaskProperty.getRecurrentTask().getNextExecution();
        return info;
    }
}
