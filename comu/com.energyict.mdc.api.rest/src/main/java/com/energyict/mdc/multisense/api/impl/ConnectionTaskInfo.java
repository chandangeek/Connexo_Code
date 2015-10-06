package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskInfo extends LinkInfo {
    public ConnectionTaskType direction;
    public LinkInfo connectionMethod;
    @XmlJavaTypeAdapter(ConnectionTaskLifecycleStateAdapter.class)
    public ConnectionTask.ConnectionTaskLifecycleStatus status;
    public String connectionType;
    public LinkInfo comPortPool;
    public Boolean isDefault;

    // Scheduled
    public List<PropertyInfo> properties;
    public ComWindowInfo comWindow;
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy connectionStrategy;
    public Boolean allowSimultaneousConnections;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo nextExecutionSpecs;
}


