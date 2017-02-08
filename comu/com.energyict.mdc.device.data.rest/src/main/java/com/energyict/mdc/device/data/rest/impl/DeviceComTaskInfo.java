/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.data.rest.CompletionCodeInfo;
import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class DeviceComTaskInfo {

    @JsonProperty("comTask")
    public ComTaskInfo comTask;
    @JsonProperty("connectionMethod")
    public String connectionMethod;
    @JsonProperty("connectionStrategy")
    public String connectionStrategy;
    @JsonProperty("connectionStrategyKey")
    public String connectionStrategyKey;
    @JsonProperty("nextCommunication")
    public Instant nextCommunication;
    @JsonProperty("lastCommunicationStart")
    public Instant lastCommunicationStart;
    @JsonProperty("urgency")
    public int urgency;
    @JsonProperty("securitySettings")
    public String securitySettings;
    @JsonProperty("protocolDialect")
    public String protocolDialect;
    @JsonProperty("temporalExpression")
    public TemporalExpressionInfo temporalExpression;
    @JsonProperty("scheduleType")
    public String scheduleType;
    @JsonProperty("scheduleName")
    public String scheduleName;
    @JsonProperty("plannedDate")
    public Instant plannedDate;
    @JsonProperty("status")
    public String status;
    @JsonProperty("scheduleTypeKey")
    public String scheduleTypeKey;
    @JsonProperty("connectionDefinedOnDevice")
    public boolean connectionDefinedOnDevice;
    @JsonProperty("latestResult")
    public CompletionCodeInfo latestResult;
    @JsonProperty("successfulFinishTime")
    public Instant successfulFinishTime;
    @JsonProperty("isOnHold")
    public boolean isOnHold;
    @JsonProperty("ignoreNextExecutionSpecsForInbound")
    public boolean ignoreNextExecutionSpecsForInbound;
}
