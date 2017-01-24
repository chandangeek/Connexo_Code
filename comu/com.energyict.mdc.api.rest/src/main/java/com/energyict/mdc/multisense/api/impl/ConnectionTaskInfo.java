package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskInfo extends LinkInfo<Long> {
    @NotNull(groups = {POST.class, PUT.class})
    public ConnectionTaskType direction;
    public LinkInfo<Long> connectionMethod;
    @XmlJavaTypeAdapter(ConnectionTaskLifecycleStateAdapter.class)
    public ConnectionTask.ConnectionTaskLifecycleStatus status;
    public String connectionType;
    public LinkInfo<Long> comPortPool;
    public LinkInfo<Long> device;
    public Boolean isDefault;

    // Scheduled
    public List<PropertyInfo> properties;
    public ComWindowInfo comWindow;
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy connectionStrategy;
    public Integer numberOfSimultaneousConnections = 1;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo nextExecutionSpecs;
}


