package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
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
    private final Provider<DeviceInfoFactory> deviceInfoFactoryProvider;

    @Inject
    public ConnectionTaskInfoFactory(
            MdcPropertyUtils mdcPropertyUtils, EngineConfigurationService engineConfigurationService,
            ConnectionTaskService connectionTaskService, ExceptionFactory exceptionFactory,
            Provider<PartialConnectionTaskInfoFactory> partialConnectionTaskInfoFactoryProvider,
            Provider<ComPortPoolInfoFactory> comPortPoolInfoFactoryProvider, Provider<DeviceInfoFactory> deviceInfoFactoryProvider) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.engineConfigurationService = engineConfigurationService;
        this.connectionTaskService = connectionTaskService;
        this.exceptionFactory = exceptionFactory;
        this.partialConnectionTaskInfoFactoryProvider = partialConnectionTaskInfoFactoryProvider;
        this.comPortPoolInfoFactoryProvider = comPortPoolInfoFactoryProvider;
        this.deviceInfoFactoryProvider = deviceInfoFactoryProvider;
    }

    public LinkInfo asLink(ConnectionTask connectionTask, Relation relation, UriInfo uriInfo) {
        ConnectionTaskInfo info = new ConnectionTaskInfo();
        copySelectedFields(info,connectionTask,uriInfo, Arrays.asList("id","version"));
        info.link = link(connectionTask,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ConnectionTask> connectionTasks, Relation relation, UriInfo uriInfo) {
        return connectionTasks.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(ConnectionTask connectionTask, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Connection task")
                .build(connectionTask.getDevice().getmRID(), connectionTask.getId());
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
        map.put("id", (connectionTaskInfo, connectionTask, uriInfo)-> {
            connectionTaskInfo.id = connectionTask.getId();
            if (connectionTaskInfo.device == null) {
                connectionTaskInfo.device = new LinkInfo();
                connectionTaskInfo.device.id = connectionTask.getDevice().getId();
            }
        });
        map.put("version", (connectionTaskInfo, connectionTask, uriInfo)-> {
            connectionTaskInfo.version = connectionTask.getVersion();
            if (connectionTaskInfo.device == null) {
                connectionTaskInfo.device = new LinkInfo();
                connectionTaskInfo.device.version = connectionTask.getDevice().getVersion();
            }
        });
        map.put("connectionMethod", (connectionTaskInfo, connectionTask, uriInfo)->
            connectionTaskInfo.connectionMethod = partialConnectionTaskInfoFactoryProvider.get().asLink(connectionTask.getPartialConnectionTask(), Relation.REF_PARENT, uriInfo));
        map.put("direction", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.direction = ConnectionTaskType.from(connectionTask));
        map.put("link", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.link = link(connectionTask, Relation.REF_SELF, uriInfo));
        map.put("status", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.status = connectionTask.getStatus());
        map.put("connectionType", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.connectionType = connectionTask.getPartialConnectionTask().getPluggableClass().getName());
        map.put("comPortPool", (connectionTaskInfo, connectionTask, uriInfo)->
            connectionTaskInfo.comPortPool = comPortPoolInfoFactoryProvider.get().asLink(connectionTask.getComPortPool(), Relation.REF_RELATION, uriInfo));
        map.put("device", (connectionTaskInfo, connectionTask, uriInfo)->
            connectionTaskInfo.device = deviceInfoFactoryProvider.get().asLink(connectionTask.getDevice(), Relation.REF_PARENT, uriInfo));
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
        map.put("numberOfSimultaneousConnections", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (connectionTask instanceof ScheduledConnectionTask) {
                connectionTaskInfo.numberOfSimultaneousConnections = ((ScheduledConnectionTask) connectionTask).getNumberOfSimultaneousConnections();
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
        scheduledConnectionTaskBuilder.setNumberOfSimultaneousConnections(info.numberOfSimultaneousConnections != null ? info.numberOfSimultaneousConnections : 1);
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
        scheduledConnectionTask.setNumberOfSimultaneousConnections(connectionTaskInfo.numberOfSimultaneousConnections != null ? connectionTaskInfo.numberOfSimultaneousConnections : 1);
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
