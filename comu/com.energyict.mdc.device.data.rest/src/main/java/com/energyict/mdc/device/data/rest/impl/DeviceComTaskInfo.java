package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.scheduling.rest.ComTaskInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

public class DeviceComTaskInfo {

    @JsonProperty("comTask")
    public ComTaskInfo comTask;
    @JsonProperty("connectionMethod")
    public String connectionMethod;
    @JsonProperty("connectionStrategy")
    public String connectionStrategy;
    @JsonProperty("nextCommunication")
    public Date nextCommunication;
    @JsonProperty("lastCommunicationStart")
    public Date lastCommunicationStart;
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
    public Date plannedDate;
    @JsonProperty("status")
    public String status;
    @JsonProperty("scheduleTypeKey")
    public String scheduleTypeKey;
    @JsonProperty("defaultDefined")
    public boolean defaultDefined;
}
