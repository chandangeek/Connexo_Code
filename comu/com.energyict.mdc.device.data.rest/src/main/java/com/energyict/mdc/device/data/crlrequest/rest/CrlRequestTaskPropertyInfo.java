package com.energyict.mdc.device.data.crlrequest.rest;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CrlRequestTaskPropertyInfo {
    public IdWithNameInfo securityAccessor;
    public String caName;
    public TimeDurationInfo timeDurationInfo;
    public Instant nextRun;
}
