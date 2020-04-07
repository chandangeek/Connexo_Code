/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.data.rest.DeviceConnectionTaskInfo;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.upl.TypedProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This Info element represents the PartialConnectionTask in the domain model
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InboundConnectionMethodInfo.class, name = "Inbound"),
        @JsonSubTypes.Type(value = ScheduledConnectionMethodInfo.class, name = "Outbound")})
public abstract class ConnectionMethodInfo<T extends ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> extends ConnectionTaskVersionInfo {

    @XmlJavaTypeAdapter(ConnectionTaskLifecycleStateAdapter.class)
    public ConnectionTask.ConnectionTaskLifecycleStatus status;
    public String connectionType;
    public String displayDirection;
    public String comPortPool;
    public boolean isDefault;
    public Integer comWindowStart;
    public Integer comWindowEnd;
    public List<PropertyInfo> properties;
    public Integer numberOfSimultaneousConnections = 1;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo nextExecutionSpecs;
    public String protocolDialect;
    public String protocolDialectDisplayName;
    public DeviceConnectionTaskInfo.ConnectionStrategyInfo connectionStrategyInfo;
    public ConnectionFunctionInfo connectionFunctionInfo;
    private Thesaurus thesaurus;
    public String errorMessage;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(ConnectionTask<?, ?> connectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this();
        this.thesaurus = thesaurus;
        this.id = connectionTask.getId();
        this.name = connectionTask.getName();
        this.status = connectionTask.getStatus();
        this.connectionType = connectionTask.getPartialConnectionTask().getPluggableClass().getName();
        this.comPortPool = connectionTask.getComPortPool() != null ? connectionTask.getComPortPool().getName() : null;
        this.isDefault = connectionTask.isDefault();
        List<PropertySpec> propertySpecs = connectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        this.properties = new ArrayList<>();
        ProtocolDialectConfigurationProperties dialectConfigurationProperties = connectionTask.getProtocolDialectConfigurationProperties();
        this.protocolDialect = dialectConfigurationProperties.getDeviceProtocolDialectName();
        DeviceProtocolDialect protocolDialect = dialectConfigurationProperties.getDeviceProtocolDialect();
        if (protocolDialect != null) {
            this.protocolDialectDisplayName = protocolDialect.getDeviceProtocolDialectDisplayName();
        }
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
        this.version = connectionTask.getVersion();
        Device device = connectionTask.getDevice();
        this.parent = new VersionInfo<>(device.getName(), device.getVersion());
        this.connectionStrategyInfo = new DeviceConnectionTaskInfo.ConnectionStrategyInfo();
        this.connectionFunctionInfo = getConnectionFunctionInfo(connectionTask);
    }

    private ConnectionFunctionInfo getConnectionFunctionInfo(ConnectionTask<?, ?> connectionTask) {
        PartialConnectionTask partialConnectionTask = connectionTask.getPartialConnectionTask();
        return partialConnectionTask.getConnectionFunction().isPresent()
                ? new ConnectionFunctionInfo(partialConnectionTask.getConnectionFunction().get())
                : deviceProtocolSupportsConnectionFunctions(partialConnectionTask.getConfiguration().getDeviceType()) ? getNoConnectionFunctionSpecifiedConnectionFunctionInfo(thesaurus) : null;
    }

    private boolean deviceProtocolSupportsConnectionFunctions(DeviceType deviceType) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClassOptional = deviceType.getDeviceProtocolPluggableClass();
        return deviceProtocolPluggableClassOptional.isPresent() && !deviceProtocolPluggableClassOptional.get().getProvidedConnectionFunctions().isEmpty();
    }

    private ConnectionFunctionInfo getNoConnectionFunctionSpecifiedConnectionFunctionInfo(Thesaurus thesaurus) {
        return new ConnectionFunctionInfo(new ConnectionFunction() {
            @Override
            public String getConnectionFunctionDisplayName() {
                return thesaurus.getString(DefaultTranslationKey.NONE.getKey(), DefaultTranslationKey.NONE.getDefaultFormat());
            }

            @Override
            public String getConnectionFunctionName() {
                return DefaultTranslationKey.NONE.getKey();
            }

            @Override
            public long getId() {
                return -1;
            }
        });
    }

    protected void writeTo(T connectionTask, PartialConnectionTask partialConnectionTask, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        try {
            if (this.properties != null) {
                for (PropertySpec propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                    if (propertyValue != null || !hasInheritedPropertyValue(partialConnectionTask, propertySpec)) {
                        if (propertyValue instanceof String) {
                            propertyValue = ((String) propertyValue).trim();
                        }
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
        return property != null && !"".equals(property);
    }

    public abstract ConnectionTask<?, ?> createTask(EngineConfigurationService engineConfigurationService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask);

    @JsonIgnore
    protected ConnectionStrategy getConnectionStrategy(DeviceConnectionTaskInfo.ConnectionStrategyInfo info) {
        try {
            return ConnectionStrategy.valueOf(info.connectionStrategy);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
