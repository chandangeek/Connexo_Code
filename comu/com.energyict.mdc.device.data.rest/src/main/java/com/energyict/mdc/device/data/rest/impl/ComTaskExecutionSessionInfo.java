package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.device.configuration.rest.DeviceConfigurationIdInfo;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.elster.jupiter.rest.util.JsonInstantAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-17 (16:34)
 */
class ComTaskExecutionSessionInfo {
    public long id;
    public String name;
    public List<IdWithNameInfo> comTasks;
    public IdWithNameInfo device;
    public DeviceConfigurationIdInfo deviceConfiguration;
    public IdWithNameInfo deviceType;
    public String comScheduleName;
    public TemporalExpressionInfo comScheduleFrequency;
    public int urgency;
    public String result;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant startTime;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant finishTime;
    public Long durationInSeconds;
    public boolean alwaysExecuteOnInbound;
    public ComSessionInfo comSession;
}