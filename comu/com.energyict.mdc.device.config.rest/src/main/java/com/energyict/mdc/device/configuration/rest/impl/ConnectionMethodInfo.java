/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import com.energyict.mdc.upl.TypedProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.ws.rs.core.UriInfo;
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
public abstract class ConnectionMethodInfo<T extends PartialConnectionTask> {

    @JsonIgnore
    protected MdcPropertyUtils mdcPropertyUtils;

    public long id;
    public String name;
    public ConnectionTypePluggableClassInfo connectionTypePluggableClass;
    public String comPortPool;
    public String displayDirection;
    public boolean isDefault;
    public Integer comWindowStart;
    public Integer comWindowEnd;
    public List<PropertyInfo> properties;
    public Integer numberOfSimultaneousConnections = 1;
    public Integer numberOfRetriesConnectionMethod = 3;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo temporalExpression;
    public long version;
    public VersionInfo<Long> parent;
    public ConnectionStrategyInfo connectionStrategyInfo;
    public ConnectionFunctionInfo connectionFunctionInfo;
    public ProtocolDialectConfigurationPropertiesInfo protocolDialectConfigurationProperties;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(PartialConnectionTask partialConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.id = partialConnectionTask.getId();
        this.name = partialConnectionTask.getName();
        this.connectionTypePluggableClass = ConnectionTypePluggableClassInfo.from(partialConnectionTask.getPluggableClass());
        this.comPortPool = partialConnectionTask.getComPortPool() != null ? partialConnectionTask.getComPortPool().getName() : null;
        this.isDefault = partialConnectionTask.isDefault();
        List<PropertySpec> propertySpecs = partialConnectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        this.properties = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
        this.version = partialConnectionTask.getVersion();
        DeviceConfiguration deviceConfiguration = partialConnectionTask.getConfiguration();
        this.parent = new VersionInfo<>(deviceConfiguration.getId(), deviceConfiguration.getVersion());
        this.connectionFunctionInfo = partialConnectionTask.getConnectionFunction().isPresent()
                ? new ConnectionFunctionInfo(partialConnectionTask.getConnectionFunction().get())
                : deviceProtocolSupportsConnectionFunctions(partialConnectionTask.getConfiguration().getDeviceType()) ? getNoConnectionFunctionSpecifiedConnectionFunctionInfo(thesaurus) : null;
        this.protocolDialectConfigurationProperties = ProtocolDialectConfigurationPropertiesInfo.from(partialConnectionTask.getProtocolDialectConfigurationProperties(), thesaurus);
    }

    private boolean deviceProtocolSupportsConnectionFunctions(DeviceType deviceType) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClassOptional = deviceType.getDeviceProtocolPluggableClass();
        return deviceProtocolPluggableClassOptional.isPresent() && !deviceProtocolPluggableClassOptional.get().getProvidedConnectionFunctions().isEmpty();
    }

    private ConnectionFunctionInfo getNoConnectionFunctionSpecifiedConnectionFunctionInfo(Thesaurus thesaurus) {
        return new ConnectionFunctionInfo(new ConnectionFunction() {
            @Override
            public String getConnectionFunctionDisplayName() {
                return thesaurus.getString(TranslationKeys.NONE.getKey(), TranslationKeys.NONE.getDefaultFormat());
            }

            @Override
            public String getConnectionFunctionName() {
                return TranslationKeys.NONE.getKey();
            }

            @Override
            public long getId() {
                return -1;
            }
        });
    }

    protected void addPropertiesToPartialConnectionTask(PartialConnectionTaskBuilder<?, ?, ?> connectionTaskBuilder, ConnectionTypePluggableClass connectionTypePluggableClass) {
        if (this.properties != null) {
            try {
                for (PropertySpec propertySpec : connectionTypePluggableClass.getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                    if (propertyValue != null) {
                        connectionTaskBuilder.addProperty(propertySpec.getName(), propertyValue);
                    }
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties." + e.getViolatingProperty());
            }
        }
    }

    protected ConnectionTypePluggableClass findConnectionTypeOrThrowException(long pluggableClassId, ProtocolPluggableService protocolPluggableService) {
        if (pluggableClassId == 0) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "connectionTypePluggableClassId");
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClass(pluggableClassId);
        return pluggableClass.orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.CONNECTION_TYPE_UNKNOWN, "connectionTypePluggableClass", pluggableClassId));
    }

    protected void writeTo(T partialConnectionTask, DeviceType deviceType, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionTypePluggableClass.id, protocolPluggableService);
        partialConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass);
        partialConnectionTask.setName(this.name);
        partialConnectionTask.setConnectionFunction(getConnectionFunction(deviceType));
    }

    protected ConnectionFunction getConnectionFunction(DeviceType deviceType) {
        return this.connectionFunctionInfo != null && deviceType.getDeviceProtocolPluggableClass().isPresent()
                ? deviceType.getDeviceProtocolPluggableClass().get()
                .getProvidedConnectionFunctions()
                .stream()
                .filter(connectionFunction -> connectionFunction.getId() == this.connectionFunctionInfo.id)
                .findFirst().orElse(null)
                : null;
    }

    public abstract PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils, Thesaurus thesaurus);

    public static class ConnectionTypePluggableClassInfo {
        public Long id;
        public String name;

        public ConnectionTypePluggableClassInfo() {
        }

        public static ConnectionTypePluggableClassInfo from(ConnectionTypePluggableClass connectionTypePluggableClass) {
            ConnectionTypePluggableClassInfo connectionTypePluggableClassInfo = new ConnectionTypePluggableClassInfo();
            if (connectionTypePluggableClass != null) {
                connectionTypePluggableClassInfo.id = connectionTypePluggableClass.getId();
                connectionTypePluggableClassInfo.name = connectionTypePluggableClass.getName();
            }
            return connectionTypePluggableClassInfo;
        }
    }

    public static class ProtocolDialectConfigurationPropertiesInfo {
        public static final Long DEFAULT_PROTOCOL_DIALECT_ID = -1L;
        public static final String DEFAULT_PROTOCOL_DIALECT_NAME_KEY = "default.protocol.dialect.name";
        public Long id;
        public String name;
        public String displayName;

        public ProtocolDialectConfigurationPropertiesInfo() {
        }

        public static ProtocolDialectConfigurationPropertiesInfo from(ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties, Thesaurus thesaurus) {
            ProtocolDialectConfigurationPropertiesInfo protocolDialectConfigurationPropertiesInfo = new ProtocolDialectConfigurationPropertiesInfo();
            if (protocolDialectConfigurationProperties == null) {
                protocolDialectConfigurationPropertiesInfo.id = DEFAULT_PROTOCOL_DIALECT_ID;
                protocolDialectConfigurationPropertiesInfo.name = thesaurus.getString(DEFAULT_PROTOCOL_DIALECT_NAME_KEY, DEFAULT_PROTOCOL_DIALECT_NAME_KEY);
                protocolDialectConfigurationPropertiesInfo.displayName = thesaurus.getString(DEFAULT_PROTOCOL_DIALECT_NAME_KEY, DEFAULT_PROTOCOL_DIALECT_NAME_KEY);
            } else {
                protocolDialectConfigurationPropertiesInfo.id = protocolDialectConfigurationProperties.getId();
                protocolDialectConfigurationPropertiesInfo.name = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDeviceProtocolDialectName();
                protocolDialectConfigurationPropertiesInfo.displayName = protocolDialectConfigurationProperties.getDeviceProtocolDialect().getDeviceProtocolDialectDisplayName();
            }
            return protocolDialectConfigurationPropertiesInfo;
        }
    }

    public static class ConnectionStrategyInfo {
        public String connectionStrategy;
        public String localizedValue;
    }

}