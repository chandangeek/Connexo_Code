package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskInfoFactory extends SelectableFieldFactory<ConnectionTaskInfo, ConnectionTask<?,?>> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final EngineConfigurationService engineConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ConnectionTaskInfoFactory(MdcPropertyUtils mdcPropertyUtils, EngineConfigurationService engineConfigurationService, ExceptionFactory exceptionFactory) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.engineConfigurationService = engineConfigurationService;
        this.exceptionFactory = exceptionFactory;
    }

    public ConnectionTaskInfo asHypermedia(ConnectionTask<?,?> connectionTask, UriInfo uriInfo, Collection<String> fields) {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        copySelectedFields(info, connectionTask, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ConnectionTaskInfo, ConnectionTask<?, ?>>> buildFieldMap() {
        Map<String, PropertyCopier<ConnectionTaskInfo, ConnectionTask<?,?>>> map = new HashMap<>();
        map.put("id", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.id = connectionTask.getId());
        map.put("connectionMethod", (connectionTaskInfo, connectionTask, uriInfo)-> {
            connectionTaskInfo.connectionMethod = new LinkInfo();
            connectionTaskInfo.connectionMethod.id = connectionTask.getPartialConnectionTask().getId();
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(PartialConnectionTaskResource.class)
                    .path(PartialConnectionTaskResource.class, "getPartialConnectionTask")
                    .resolveTemplate("deviceTypeId", connectionTask.getDevice().getDeviceConfiguration().getDeviceType().getId())
                    .resolveTemplate("deviceConfigId", connectionTask.getDevice().getDeviceConfiguration().getId())
                    .resolveTemplate("id", connectionTask.getPartialConnectionTask().getId());

            connectionTaskInfo.connectionMethod.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_PARENT).build();
        });
        map.put("direction", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.direction = ConnectionTaskType.from(connectionTask));
        map.put("link", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.link =
                Link.fromUriBuilder(
                        uriInfo.getBaseUriBuilder().
                                path(ConnectionTaskResource.class).
                                path(ConnectionTaskResource.class, "getConnectionTask")).
                    rel(LinkInfo.REF_SELF).
                    build(connectionTask.getDevice().getmRID(), connectionTask.getId())
        );
        map.put("status", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.status = connectionTask.getStatus());
        map.put("connectionType", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.connectionType = connectionTask.getPartialConnectionTask().getPluggableClass().getName());
        map.put("comPortPool", (connectionTaskInfo, connectionTask, uriInfo)-> {
            connectionTaskInfo.comPortPool = new LinkInfo();
            connectionTaskInfo.comPortPool.id = connectionTask.getComPortPool().getId();
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(ComPortPoolResource.class).
                    path(ComPortPoolResource.class, "getComPortPool");
            connectionTaskInfo.comPortPool.link = Link.fromUriBuilder(uriBuilder).rel("related").build(connectionTaskInfo.comPortPool.id);
        });
        map.put("isDefault", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.isDefault = connectionTask.isDefault());
        map.put("properties", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(connectionTask.getConnectionType().getPropertySpecs(), connectionTask.getTypedProperties()));
        map.put("comWindow", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                ComWindow communicationWindow = ((ScheduledConnectionTask) connectionTask).getCommunicationWindow();
                if (communicationWindow!=null) {
                    connectionTaskInfo.comWindow = new ComWindowInfo();
                    connectionTaskInfo.comWindow.start = communicationWindow.getStart()!=null?communicationWindow.getStart().getMillis():null;
                    connectionTaskInfo.comWindow.end = communicationWindow.getEnd()!=null?communicationWindow.getEnd().getMillis():null;
                }
            }
        });
        map.put("connectionStrategy", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                connectionTaskInfo.connectionStrategy = ((ScheduledConnectionTask) connectionTask).getConnectionStrategy();
            }
        });
        map.put("allowSimultaneousConnections", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                connectionTaskInfo.allowSimultaneousConnections = ((ScheduledConnectionTask) connectionTask).isSimultaneousConnectionsAllowed();
            }
        });
        map.put("rescheduleRetryDelay", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                TimeDuration rescheduleDelay = ((ScheduledConnectionTask) connectionTask).getRescheduleDelay();
                if (rescheduleDelay!=null) {
                    connectionTaskInfo.rescheduleRetryDelay = new TimeDurationInfo(rescheduleDelay);
                }
            }
        });
        map.put("nextExecutionSpecs", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                NextExecutionSpecs nextExecutionSpecs = ((ScheduledConnectionTask) connectionTask).getNextExecutionSpecs();
                if (nextExecutionSpecs!=null) {
                    connectionTaskInfo.nextExecutionSpecs = TemporalExpressionInfo.from(nextExecutionSpecs.getTemporalExpression());
                }
            }
        });
        return map;
    }

    public InboundConnectionTask createInboundConnectionTask(ConnectionTaskInfo info, Device device, PartialConnectionTask partialConnectionTask) {
        if (!(partialConnectionTask instanceof PartialInboundConnectionTask)) {
            throw exceptionFactory.newException(MessageSeeds.EXPECTED_PARTIAL_INBOUND);
        }
        PartialInboundConnectionTask partialInboundConnectionTask = (PartialInboundConnectionTask) partialConnectionTask;
        Device.InboundConnectionTaskBuilder inboundConnectionTaskBuilder = device.getInboundConnectionTaskBuilder(partialInboundConnectionTask);
        if (info.comPortPool!=null && info.comPortPool.id!=null) {
            engineConfigurationService
                    .findInboundComPortPool(info.comPortPool.id)
                    .ifPresent(inboundConnectionTaskBuilder::setComPortPool);
        }
        inboundConnectionTaskBuilder.setConnectionTaskLifecycleStatus(info.status);
        return inboundConnectionTaskBuilder.add();
    }


    public ScheduledConnectionTask createOutboundConnectionTask(ConnectionTaskInfo info, Device device, PartialConnectionTask partialConnectionTask) {
        if (!PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
            throw new WebApplicationException("Expected partial connection task to be 'Outbound'", Response.Status.BAD_REQUEST);
        }

        PartialScheduledConnectionTask partialScheduledConnectionTask = (PartialScheduledConnectionTask) partialConnectionTask;
        Device.ScheduledConnectionTaskBuilder scheduledConnectionTaskBuilder = device.getScheduledConnectionTaskBuilder(partialScheduledConnectionTask);
        if (info.comPortPool!=null && info.comPortPool.id!=null) {
            engineConfigurationService
                    .findOutboundComPortPool(info.comPortPool.id)
                    .ifPresent(scheduledConnectionTaskBuilder::setComPortPool);
        }
        scheduledConnectionTaskBuilder.setConnectionStrategy(info.connectionStrategy);
        scheduledConnectionTaskBuilder.setNextExecutionSpecsFrom(info.nextExecutionSpecs != null ? info.nextExecutionSpecs.asTemporalExpression() : null);
        scheduledConnectionTaskBuilder.setConnectionTaskLifecycleStatus(info.status);
        scheduledConnectionTaskBuilder.setSimultaneousConnectionsAllowed(info.allowSimultaneousConnections);
        if (info.comWindow!=null && info.comWindow.end != null && info.comWindow.start != null) {
            scheduledConnectionTaskBuilder.setCommunicationWindow(new ComWindow(info.comWindow.start, info.comWindow.end));
        }

        if (info.properties != null) {
            for (PropertySpec propertySpec : partialConnectionTask.getPluggableClass().getPropertySpecs()) {
                Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, info.properties);
                if (propertyValue != null) {
                    scheduledConnectionTaskBuilder.setProperty(propertySpec.getName(), propertyValue);
                } else {
                    scheduledConnectionTaskBuilder.setProperty(propertySpec.getName(), null);
                }
            }
        }
        return scheduledConnectionTaskBuilder.add();
    }

}
