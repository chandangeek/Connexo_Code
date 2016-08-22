package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Created by bvn on 7/15/15.
 */
public class PartialConnectionTaskInfo extends LinkInfo<Long> {
    public String name;
    public ConnectionTaskType direction;
    public String connectionType;
    public LinkInfo comPortPool;
    public Boolean isDefault;
    public List<PropertyInfo> properties;
    public ComWindowInfo comWindow;
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy connectionStrategy;
    public Integer numberOfSimultaneousConnections = 1;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo nextExecutionSpecs;
}
