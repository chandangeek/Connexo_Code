package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.device.configuration.rest.ConnectionStrategyAdapter;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * This Info element represents the PartialConnectionTask in the domain model
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
     @JsonSubTypes.Type(value = InboundConnectionMethodInfo.class, name = "Inbound"),
     @JsonSubTypes.Type(value = ScheduledConnectionMethodInfo.class, name = "Outbound") })
public abstract class ConnectionMethodInfo<T extends ConnectionTask<?,?>> {

    @JsonIgnore
    protected MdcPropertyUtils mdcPropertyUtils;

    public long id;
    public String name;
    public String connectionType;
    public String comPortPool;
    public boolean isDefault;
    public int comWindowStart;
    public int comWindowEnd;
    @XmlJavaTypeAdapter(ConnectionStrategyAdapter.class)
    public ConnectionStrategy connectionStrategy;
    public List<PropertyInfo> properties;
    public boolean allowSimultaneousConnections;
    public TimeDurationInfo rescheduleRetryDelay;
    public TemporalExpressionInfo temporalExpression;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(ConnectionTask<?,?> connectionTask, UriInfo uriInfo, MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.id=connectionTask.getId();
        this.name= connectionTask.getName();
        this.connectionType= connectionTask.getConnectionType().toString();
        this.comPortPool= connectionTask.getComPortPool()!=null?connectionTask.getComPortPool().getName():null;
        this.isDefault= connectionTask.isDefault();
        List<PropertySpec> propertySpecs = connectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = connectionTask.getTypedProperties();
        this.properties = new ArrayList<>();
        mdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
    }

    protected void addPropertiesToPartialConnectionTask(PartialConnectionTaskBuilder<?, ?, ?> connectionTaskBuilder, ConnectionTypePluggableClass connectionTypePluggableClass) {
        if (this.properties !=null) {
            for (PropertySpec<?> propertySpec : connectionTypePluggableClass.getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
                if (propertyValue!=null) {
                    connectionTaskBuilder.addProperty(propertySpec.getName(), propertyValue);
                }
            }
        }
    }

    protected ConnectionTypePluggableClass findConnectionTypeOrThrowException(String pluggableClassName, ProtocolPluggableService protocolPluggableService) {
        ConnectionTypePluggableClass pluggableClass = protocolPluggableService.findConnectionTypePluggableClassByName(pluggableClassName);
        if (pluggableClass==null) {
            throw new WebApplicationException("No such connection type", Response.status(Response.Status.NOT_FOUND).entity("No such connection type").build());
        }
        return pluggableClass;
    }

    protected void writeTo(T connectionTask, EngineModelService engineModelService) {
//        connectionTask.setName(this.name);
    }


    public abstract ConnectionTask<?,?> createTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService, MdcPropertyUtils mdcPropertyUtils);
}
