package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.TimeDurationInfo;
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
        TemporalExpression temporalExpression = (TemporalExpression) crlRequestTaskProperty.getRecurrentTask().getScheduleExpression();
        info.timeDurationInfo = TimeDurationInfo.of(temporalExpression.getEvery());
        info.nextRun = crlRequestTaskProperty.getRecurrentTask().getNextExecution().toEpochMilli();
        return info;
    }
}
