package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
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
     @JsonSubTypes.Type(value = ScheduledConnectionMethodInfo.class, name = "Outbound") })
public abstract class ConnectionMethodInfo<T extends PartialConnectionTask> {

    @JsonIgnore
    protected MdcPropertyUtils mdcPropertyUtils;

    public long id;
    public String name;
    public String connectionTypePluggableClass;
    public String comPortPool;
    public boolean isDefault;
    public Integer comWindowStart;
    public Integer comWindowEnd;
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy connectionStrategy;
    public List<PropertyInfo> properties;
    public boolean allowSimultaneousConnections;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo temporalExpression;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(PartialConnectionTask partialConnectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.id=partialConnectionTask.getId();
        this.name= partialConnectionTask.getName();
        this.connectionTypePluggableClass = partialConnectionTask.getPluggableClass().getName();
        this.comPortPool= partialConnectionTask.getComPortPool()!=null?partialConnectionTask.getComPortPool().getName():null;
        this.isDefault= partialConnectionTask.isDefault();
        List<PropertySpec> propertySpecs = partialConnectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        this.properties = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
    }

    protected void addPropertiesToPartialConnectionTask(PartialConnectionTaskBuilder<?, ?, ?> connectionTaskBuilder, ConnectionTypePluggableClass connectionTypePluggableClass) {
        if (this.properties !=null) {
            try {
                for (PropertySpec propertySpec : connectionTypePluggableClass.getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                    if (propertyValue != null) {
                        connectionTaskBuilder.addProperty(propertySpec.getName(), propertyValue);
                    }
                }
            } catch (LocalizedFieldValidationException e) {
                throw new LocalizedFieldValidationException(e.getMessageSeed(), "properties."+e.getViolatingProperty());
            }
        }
    }

    protected ConnectionTypePluggableClass findConnectionTypeOrThrowException(String pluggableClassName, ProtocolPluggableService protocolPluggableService) {
        if (Checks.is(pluggableClassName).emptyOrOnlyWhiteSpace()) {
            throw new LocalizedFieldValidationException(MessageSeeds.FIELD_IS_REQUIRED, "connectionTypePluggableClass");
        }
        Optional<ConnectionTypePluggableClass> pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName(pluggableClassName);
        return pluggableClass.orElseThrow(() -> new LocalizedFieldValidationException(MessageSeeds.CONNECTION_TYPE_UNKNOWN, "connectionTypePluggableClass", pluggableClassName));
    }

    protected void writeTo(T partialConnectionTask, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService) {
        ConnectionTypePluggableClass connectionTypePluggableClass = findConnectionTypeOrThrowException(this.connectionTypePluggableClass, protocolPluggableService);
        partialConnectionTask.setConnectionTypePluggableClass(connectionTypePluggableClass);
        partialConnectionTask.setName(this.name);
    }


    public abstract PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineConfigurationService engineConfigurationService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils);
}
