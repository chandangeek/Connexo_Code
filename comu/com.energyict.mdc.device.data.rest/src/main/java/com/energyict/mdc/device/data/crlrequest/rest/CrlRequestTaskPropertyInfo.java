package com.energyict.mdc.device.data.crlrequest.rest;

import com.elster.jupiter.time.rest.TimeDurationInfo;

import java.util.List;

public class CrlRequestTaskPropertyInfo {
    public long recurrentTaskId;
    public String recurrentTaskName;
    public String securityAccessorName;
    public List<String> securityAccessorNames;
    public String caName;
    public TimeDurationInfo timeDurationInfo;
    public Long nextRun;
}
