package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.ConnectionTaskService;
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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskInfoFactory extends SelectableFieldFactory<ConnectionTaskInfo, ConnectionTask<?,?>> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final EngineConfigurationService engineConfigurationService;
    private final ConnectionTaskService connectionTaskService;
    private final ExceptionFactory exceptionFactory;
    private final Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider;
    private final Provider<ComPortPoolInfoFactory> comPortPoolInfoFactoryProvider;

    @Inject
    public ConnectionTaskInfoFactory(
            MdcPropertyUtils mdcPropertyUtils, EngineConfigurationService engineConfigurationService,
            ConnectionTaskService connectionTaskService, ExceptionFactory exceptionFactory,
            Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider,
            Provider<ComPortPoolInfoFactory> comPortPoolInfoFactoryProvider) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.engineConfigurationService = engineConfigurationService;
        this.connectionTaskService = connectionTaskService;
        this.exceptionFactory = exceptionFactory;
        this.partialConnectionTaskInfoFactoryProvider = partialConnectionTaskInfoFactoryProvider;
        this.comPortPoolInfoFactoryProvider = comPortPoolInfoFactoryProvider;
    }

    public LinkInfo asLink(ConnectionTask connectionTask, Relation relation, UriInfo uriInfo) {
        return asLink(connectionTask, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<ConnectionTask> connectionTasks, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return connectionTasks.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(ConnectionTask connectionTask, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = connectionTask.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Connection task")
                .build(connectionTask.getDevice().getmRID(), connectionTask.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(ConnectionTaskResource.class)
                .path(ConnectionTaskResource.class, "getConnectionTask");
    }

    public ConnectionTaskInfo from(ConnectionTask<?, ?> connectionTask, UriInfo uriInfo, Collection<String> fields) {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        copySelectedFields(info, connectionTask, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ConnectionTaskInfo, ConnectionTask<?, ?>>> buildFieldMap() {
        Map<String, PropertyCopier<ConnectionTaskInfo, ConnectionTask<?,?>>> map = new HashMap<>();
        map.put("id", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.id = connectionTask.getId());
        map.put("connectionMethod", (connectionTaskInfo, connectionTask, uriInfo)->
            connectionTaskInfo.connectionMethod = partialConnectionTaskInfoFactoryProvider.get().asLink(connectionTask.getPartialConnectionTask(), Relation.REF_PARENT, uriInfo));
        map.put("direction", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.direction = ConnectionTaskType.from(connectionTask));
        map.put("link", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.link = this.asLink(connectionTask, Relation.REF_SELF, uriInfo).link);
        map.put("status", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.status = connectionTask.getStatus());
        map.put("connectionType", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.connectionType = connectionTask.getPartialConnectionTask().getPluggableClass().getName());
        map.put("comPortPool", (connectionTaskInfo, connectionTask, uriInfo)->
            connectionTaskInfo.comPortPool = comPortPoolInfoFactoryProvider.get().asLink(connectionTask.getComPortPool(), Relation.REF_RELATION, uriInfo));
        map.put("isDefault", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.isDefault = connectionTask.isDefault());
        map.put("properties", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(connectionTask.getConnectionType().getPropertySpecs(), connectionTask.getTypedProperties()));
        map.put("comWindow", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (connectionTask instanceof ScheduledConnectionTask) {
                ComWindow communicationWindow = ((ScheduledConnectionTask) connectionTask).getCommunicationWindow();
                if (communicationWindow!=null) {
                    connectionTaskInfo.comWindow = new ComWindowInfo();
                    connectionTaskInfo.comWindow.start = communicationWindow.getStart()!=null?communicationWindow.getStart().getMillis():null;
                    connectionTaskInfo.comWindow.end = communicationWindow.getEnd()!=null?communicationWindow.getEnd().getMillis():null;
                }
            }
        });
        map.put("connectionStrategy", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (connectionTask instanceof ScheduledConnectionTask) {
                connectionTaskInfo.connectionStrategy = ((ScheduledConnectionTask) connectionTask).getConnectionStrategy();
            }
        });
        map.put("allowSimultaneousConnections", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (connectionTask instanceof ScheduledConnectionTask) {
                connectionTaskInfo.allowSimultaneousConnections = ((ScheduledConnectionTask) connectionTask).isSimultaneousConnectionsAllowed();
            }
        });
        map.put("rescheduleRetryDelay", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (connectionTask instanceof ScheduledConnectionTask) {
                TimeDuration rescheduleDelay = ((ScheduledConnectionTask) connectionTask).getRescheduleDelay();
                if (rescheduleDelay!=null) {
                    connectionTaskInfo.rescheduleRetryDelay = new TimeDurationInfo(rescheduleDelay);
                }
            }
        });
        map.put("nextExecutionSpecs", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (connectionTask instanceof ScheduledConnectionTask) {
                NextExecutionSpecs nextExecutionSpecs = ((ScheduledConnectionTask) connectionTask).getNextExecutionSpecs();
                if (nextExecutionSpecs != null) {
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


    public ScheduledConnectionTask createScheduledConnectionTask(ConnectionTaskInfo info, Device device, PartialConnectionTask partialConnectionTask) {
        if (!(partialConnectionTask instanceof PartialScheduledConnectionTask)) {
            throw exceptionFactory.newException(MessageSeeds.EXPECTED_PARTIAL_OUTBOUND);
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
        scheduledConnectionTaskBuilder.setSimultaneousConnectionsAllowed(info.allowSimultaneousConnections!=null?info.allowSimultaneousConnections:false);
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

    public ConnectionTask<?, ?> updateInboundConnectionTask(long connectionTaskId, ConnectionTaskInfo connectionTaskInfo, Device device, ConnectionTask connectionTask) {
        if (!(connectionTask instanceof InboundConnectionTask)) {
            throw exceptionFactory.newException(MessageSeeds.EXPECTED_INBOUND);
        }
        InboundConnectionTask inboundConnectionTask = (InboundConnectionTask) connectionTask;
        setPropertiesTo(connectionTaskInfo, inboundConnectionTask);
        if (connectionTaskInfo.comPortPool==null || connectionTaskInfo.comPortPool.id==null) {
            inboundConnectionTask.setComPortPool(null);
        } else {
            inboundConnectionTask.setComPortPool(engineConfigurationService.findInboundComPortPool(connectionTaskInfo.comPortPool.id).orElse(null));
        }
        inboundConnectionTask.save();
        pauseOrResumeTask(connectionTaskInfo, inboundConnectionTask);
        updateDefaultStatus(connectionTaskInfo, device, inboundConnectionTask);
        return connectionTaskService.findConnectionTask(connectionTaskId).get();
    }

    public ConnectionTask<?, ?> updateScheduledConnectionTask(long connectionTaskId, ConnectionTaskInfo connectionTaskInfo, Device device, ConnectionTask connectionTask) {
        if (!(connectionTask instanceof ScheduledConnectionTask)) {
            throw exceptionFactory.newException(MessageSeeds.EXPECTED_OUTBOUND);
        }
        ScheduledConnectionTask scheduledConnectionTask = (ScheduledConnectionTask) connectionTask;
        setPropertiesTo(connectionTaskInfo, scheduledConnectionTask);
        scheduledConnectionTask.setSimultaneousConnectionsAllowed(connectionTaskInfo.allowSimultaneousConnections!=null?connectionTaskInfo.allowSimultaneousConnections:false);
        if (connectionTaskInfo.comWindow!=null && connectionTaskInfo.comWindow.start != null && connectionTaskInfo.comWindow.end!=null) {
            scheduledConnectionTask.setCommunicationWindow(new ComWindow(connectionTaskInfo.comWindow.start, connectionTaskInfo.comWindow.end));
        } else {
            scheduledConnectionTask.setCommunicationWindow(null);
        }
        if (connectionTaskInfo.comPortPool==null || connectionTaskInfo.comPortPool.id==null) {
            scheduledConnectionTask.setComPortPool(null);
        } else {
            scheduledConnectionTask.setComPortPool(engineConfigurationService.findOutboundComPortPool(connectionTaskInfo.comPortPool.id).orElse(null));
        }
        scheduledConnectionTask.setConnectionStrategy(connectionTaskInfo.connectionStrategy);
        try {
            scheduledConnectionTask.setNextExecutionSpecsFrom(connectionTaskInfo.nextExecutionSpecs != null ? connectionTaskInfo.nextExecutionSpecs.asTemporalExpression() : null);
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField("nextExecutionSpecs");
        }
        scheduledConnectionTask.save();
        pauseOrResumeTask(connectionTaskInfo, scheduledConnectionTask);
        updateDefaultStatus(connectionTaskInfo, device, scheduledConnectionTask);
        return connectionTaskService.findConnectionTask(connectionTaskId).get();
    }

    private void setPropertiesTo(ConnectionTaskInfo connectionTaskInfo, ConnectionTask<?,?> connectionTask) {
        try {
            if (connectionTaskInfo.properties != null) {
                for (PropertySpec propertySpec : connectionTask.getPartialConnectionTask().getPluggableClass().getPropertySpecs()) {
                    Object propertyValue = mdcPropertyUtils.findPropertyValue(propertySpec, connectionTaskInfo.properties);
                    if (propertyValue != null) {
                        connectionTask.setProperty(propertySpec.getName(), propertyValue);
                    } else {
                        connectionTask.removeProperty(propertySpec.getName());
                    }
                }
            }
        } catch (LocalizedFieldValidationException e) {
            throw e.fromSubField("properties");
        }
    }

    private void updateDefaultStatus(ConnectionTaskInfo connectionTaskInfo, Device device, ConnectionTask<?, ?> connectionTask) {
        if (connectionTaskInfo.isDefault==null) {
            connectionTaskInfo.isDefault=false;
        }
        if (connectionTaskInfo.isDefault && !connectionTask.isDefault()) {
            connectionTaskService.setDefaultConnectionTask(connectionTask);
        } else if (!connectionTaskInfo.isDefault && connectionTask.isDefault()) {
            connectionTaskService.clearDefaultConnectionTask(device);
        }
    }

    private void pauseOrResumeTask(ConnectionTaskInfo connectionTaskInfo, ConnectionTask<?, ?> task) {
        switch (connectionTaskInfo.status) {
            case ACTIVE:
                task.activate();
                break;
            case INACTIVE:
                task.deactivate();
                break;
        }
    }

}