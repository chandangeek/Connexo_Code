/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This Info element represents the PartialConnectionTask in the domain model
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InboundConnectionMethodInfo.class, name = "Inbound"),
        @JsonSubTypes.Type(value = ScheduledConnectionMethodInfo.class, name = "Outbound")})
public abstract class ConnectionMethodInfo<T extends ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> extends ConnectionTaskVersionInfo{

    @XmlJavaTypeAdapter(ConnectionTaskLifecycleStateAdapter.class)
    public ConnectionTask.ConnectionTaskLifecycleStatus status;
    public String connectionType;
    public String displayDirection;
    public String comPortPool;
    public boolean isDefault;
    public Integer comWindowStart;
    public Integer comWindowEnd;
    public String connectionStrategy;
    public List<PropertyInfo> properties;
    public Integer numberOfSimultaneousConnections = 1;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo nextExecutionSpecs;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(ConnectionTask<?, ?> connectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        this();
        this.id = connectionTask.getId();
        this.name = connectionTask.getName();
        this.status = connectionTask.getStatus();
        this.connectionType = connectionTask.getPartialConnectionTask().getPluggableClass().getName();
        this.comPortPool = connectionTask.getComPortPool() != null ? connectionTask.getComPortPool().getName() : null;
        this.isDefault = connectionTask.isDefault();
        List<PropertySpec> propertySpecs = connectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        this.properties = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
        this.version = connectionTask.getVersion();
        Device device = connectionTask.getDevice();
        this.parent = new VersionInfo<>(device.getName(), device.getVersion());
    }


    protected void writeTo(T connectionTask, PartialConnectionTask partialConnectionTask, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        try {
            if (this.properties != null) {
                for (PropertySpec propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                    if (propertyValue != null || !hasInheritedPropertyValue(partialConnectionTask, propertySpec)) {
                        connectionTask.setProperty(propertySpec.getName(), propertyValue);
                    } else {
                        connectionTask.removeProperty(propertySpec.getName());//it means that we really want to use inherited value
                    }
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField("properties");
        }
    }

    protected boolean hasInheritedPropertyValue(PartialConnectionTask partialConnectionTask, PropertySpec propertySpec) {
        Object property = partialConnectionTask.getTypedProperties().getProperty(propertySpec.getName());
        return property !=null && !"".equals(property);
    }

    public abstract ConnectionTask<?, ?> createTask(EngineConfigurationService engineConfigurationService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask);

    @JsonIgnore
    protected ConnectionStrategy getConnectionStrategy(){
        return Arrays.stream(ConnectionStrategy.values())
                .filter(candidate -> candidate.name().equals(this.connectionStrategy))
                .findFirst()
                .orElse(null);
    }
}