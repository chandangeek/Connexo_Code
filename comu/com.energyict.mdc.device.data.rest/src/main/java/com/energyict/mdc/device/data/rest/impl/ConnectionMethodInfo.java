package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.rest.ConnectionTaskLifecycleStateAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.engine.config.ComPortPool;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
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
public abstract class ConnectionMethodInfo<T extends ConnectionTask<? extends ComPortPool, ? extends PartialConnectionTask>> {

    public long id;
    public String name;
    @XmlJavaTypeAdapter(ConnectionTaskLifecycleStateAdapter.class)
    public ConnectionTask.ConnectionTaskLifecycleStatus status;
    public String connectionType;
    public String comPortPool;
    public boolean isDefault;
    public Integer comWindowStart;
    public Integer comWindowEnd;
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy connectionStrategy;
    public List<PropertyInfo> properties;
    public boolean allowSimultaneousConnections;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo nextExecutionSpecs;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(ConnectionTask<?, ?> connectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
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
    }


    protected void writeTo(T connectionTask, PartialConnectionTask partialConnectionTask, EngineConfigurationService engineConfigurationService, MdcPropertyUtils mdcPropertyUtils) {
        try {
            if (this.properties != null) {
                for (PropertySpec propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                    if (propertyValue != null) {
                        connectionTask.setProperty(propertySpec.getName(), propertyValue);
                    } else {
                        Optional<PropertyValueInfo<?>> propertyValueInfo = findPropertyValueInfo(this.properties, propertySpec.getName());
                        if (propertyValueInfo.isPresent() && (propertyValueInfo.get().inheritedValue == null || "".equals(propertyValueInfo.get().inheritedValue))) {
                            connectionTask.setProperty(propertySpec.getName(), null);
                        } else {
                            connectionTask.removeProperty(propertySpec.getName());//it means that we really want to use inherited value
                        }
                    }
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField("properties");
        }
    }

    protected Optional<PropertyValueInfo<?>> findPropertyValueInfo(List<PropertyInfo> properties, String key) {
        return properties.stream().filter(propertyInfo -> key.equals(propertyInfo.key)).map(PropertyInfo::getPropertyValueInfo).findFirst();
    }

    public abstract ConnectionTask<?, ?> createTask(EngineConfigurationService engineConfigurationService, Device device, MdcPropertyUtils mdcPropertyUtils, PartialConnectionTask partialConnectionTask);

}