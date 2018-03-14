package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.time.PeriodicalScheduleExpression;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.elster.jupiter.util.time.ScheduleExpression;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfoFactory;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;

import javax.inject.Inject;

public class CrlRequestTaskInfoFactory {
    private final SecurityAccessorInfoFactory securityAccessorInfoFactory;

    @Inject
    public CrlRequestTaskInfoFactory(SecurityAccessorInfoFactory securityAccessorInfoFactory) {
        this.securityAccessorInfoFactory = securityAccessorInfoFactory;
    }

    public CrlRequestTaskPropertyInfo asInfo(CrlRequestTaskProperty crlRequestTaskProperty) {
        CrlRequestTaskPropertyInfo info = new CrlRequestTaskPropertyInfo();
        info.recurrentTaskId = crlRequestTaskProperty.getRecurrentTask().getId();
        info.recurrentTaskName = crlRequestTaskProperty.getRecurrentTask().getName();
        info.securityAccessorInfo = securityAccessorInfoFactory.from(crlRequestTaskProperty.getSecurityAccessor());
        info.caName = crlRequestTaskProperty.getCaName();
        ScheduleExpression scheduleExpression = crlRequestTaskProperty.getRecurrentTask().getScheduleExpression();
        info.schedule = scheduleExpression instanceof TemporalExpression ? new PeriodicalExpressionInfo((TemporalExpression) scheduleExpression) : PeriodicalExpressionInfo.from((PeriodicalScheduleExpression) scheduleExpression);
        return info;
    }
}
