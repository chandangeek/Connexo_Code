package com.energyict.mdc.device.data.crlrequest.rest;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;

public class CrlRequestTaskPropertyInfo {
    public long recurrentTaskId;
    public String recurrentTaskName;
    public SecurityAccessorInfo securityAccessorInfo;
    public String caName;
    public PeriodicalExpressionInfo schedule;
}
