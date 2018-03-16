package com.energyict.mdc.device.data.crlrequest.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.crlrequest.CrlRequestTaskProperty;
import com.energyict.mdc.device.data.crlrequest.rest.CrlRequestTaskPropertyInfo;

public class CrlRequestTaskInfoFactory {

    public CrlRequestTaskInfoFactory() {
    }

    public CrlRequestTaskPropertyInfo asInfo(CrlRequestTaskProperty crlRequestTaskProperty) {
        CrlRequestTaskPropertyInfo info = new CrlRequestTaskPropertyInfo();
        info.securityAccessor = new IdWithNameInfo(crlRequestTaskProperty.getSecurityAccessor().getKeyAccessorType().getId(), crlRequestTaskProperty.getSecurityAccessor().getKeyAccessorType().getName());
        info.caName = crlRequestTaskProperty.getCaName();
        TemporalExpression temporalExpression = (TemporalExpression) crlRequestTaskProperty.getRecurrentTask().getScheduleExpression();
        info.timeDurationInfo = TimeDurationInfo.of(temporalExpression.getEvery());
        info.nextRun = crlRequestTaskProperty.getRecurrentTask().getNextExecution().toEpochMilli();
        return info;
    }
}
