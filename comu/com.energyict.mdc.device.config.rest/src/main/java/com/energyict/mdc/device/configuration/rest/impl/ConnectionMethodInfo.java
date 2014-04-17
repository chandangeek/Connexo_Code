package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskBuilder;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyInfo;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * This Info element represents the PartialConnectionTask in the domain model
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "direction")
@JsonSubTypes({
     @JsonSubTypes.Type(value = InboundConnectionMethodInfo.class, name = "Inbound"),
     @JsonSubTypes.Type(value = ScheduledConnectionMethodInfo.class, name = "Outbound") })
public abstract class ConnectionMethodInfo<T extends PartialConnectionTask> {
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
    public NextExecutionsSpecsInfo nextExecutionSpecs;

    public ConnectionMethodInfo() {
    }

    protected ConnectionMethodInfo(PartialConnectionTask partialConnectionTask, UriInfo uriInfo) {
        this.id=partialConnectionTask.getId();
        this.name= partialConnectionTask.getName();
        this.connectionType= partialConnectionTask.getPluggableClass().getName();
        this.comPortPool= partialConnectionTask.getComPortPool()!=null?partialConnectionTask.getComPortPool().getName():null;
        this.isDefault= partialConnectionTask.isDefault();
        List<PropertySpec> propertySpecs = partialConnectionTask.getConnectionType().getPropertySpecs();
        TypedProperties typedProperties = partialConnectionTask.getTypedProperties();
        this.properties = new ArrayList<>();
        MdcPropertyUtils.convertPropertySpecsToPropertyInfos(uriInfo, propertySpecs, typedProperties, this.properties);
    }

    protected void addPropertiesToPartialConnectionTask(PartialConnectionTaskBuilder<?, ?, ?> connectionTaskBuilder, ConnectionTypePluggableClass connectionTypePluggableClass) {
        if (this.properties !=null) {
            for (PropertySpec propertySpec : connectionTypePluggableClass.getPropertySpecs()) {
                Object propertyValue = MdcPropertyUtils.findPropertyValue(propertySpec, this.properties);
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

    protected void writeTo(T partialConnectionTask, EngineModelService engineModelService) {
        partialConnectionTask.setName(this.name);
    }


    public abstract PartialConnectionTask createPartialTask(DeviceConfiguration deviceConfiguration, EngineModelService engineModelService, ProtocolPluggableService protocolPluggableService);
}
