package com.energyict.mdc.device.data.crlrequest.rest;

import com.elster.jupiter.time.rest.PeriodicalExpressionInfo;

import java.time.Instant;
import java.util.List;

public class CrlRequestTaskPropertyInfo {
    public long recurrentTaskId;
    public String recurrentTaskName;
    public String securityAccessorName;
    public List<String> securityAccessorNames;
    public String caName;
    public PeriodicalExpressionInfo schedule;
    public Instant nextRun;
}
