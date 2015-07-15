package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.InboundConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledConnectionTask;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.rest.TemporalExpressionInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Created by bvn on 7/13/15.
 */
public class ConnectionTaskInfoFactory extends SelectableFieldFactory<ConnectionTaskInfo, ConnectionTask<?,?>>{

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public ConnectionTaskInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public ConnectionTaskInfo asHypermedia(ConnectionTask<?,?> connectionTask, UriInfo uriInfo, Collection<String> fields) {
        ConnectionTaskInfo info = asInfo(connectionTask);
        copySelectedFields(info, connectionTask, uriInfo, fields);
        return info;
    }

    private ConnectionTaskInfo asInfo(ConnectionTask<?,?> connectionTask) {
        if (InboundConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
            return new InboundConnectionTaskInfo();
        } else if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
            return new ScheduledConnectionTaskInfo();
        } else {
            throw new IllegalArgumentException("Unsupported ConnectionMethod type "+connectionTask.getClass().getSimpleName());
        }
    }

    @Override
    protected Map<String, PropertyCopier<ConnectionTaskInfo, ConnectionTask<?, ?>>> buildFieldMap() {
        Map<String, PropertyCopier<ConnectionTaskInfo, ConnectionTask<?,?>>> map = new HashMap<>();
        map.put("id", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.id = connectionTask.getId());
        map.put("name", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.name = connectionTask.getName());
        map.put("link", (connectionTaskInfo, connectionTask, uriInfo)-> connectionTaskInfo.link = 
                Link.fromUriBuilder(
                        uriInfo.getBaseUriBuilder().
                                path(ConnectionTaskResource.class).
                                path(ConnectionTaskResource.class, "getConnectionTask")).
                    rel("self").
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
                ScheduledConnectionTaskInfo scheduledConnectionMethodInfo = (ScheduledConnectionTaskInfo) connectionTaskInfo;
                ComWindow communicationWindow = ((ScheduledConnectionTask) connectionTask).getCommunicationWindow();
                if (communicationWindow!=null) {
                    scheduledConnectionMethodInfo.comWindow = new ComWindowInfo();
                    scheduledConnectionMethodInfo.comWindow.start = communicationWindow.getStart()!=null?communicationWindow.getStart().getMillis():null;
                    scheduledConnectionMethodInfo.comWindow.end = communicationWindow.getEnd()!=null?communicationWindow.getEnd().getMillis():null;
                }
            }
        });
        map.put("connectionStrategy", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                ScheduledConnectionTaskInfo scheduledConnectionMethodInfo = (ScheduledConnectionTaskInfo) connectionTaskInfo;
                scheduledConnectionMethodInfo.connectionStrategy = ((ScheduledConnectionTask) connectionTask).getConnectionStrategy();
            }
        });
        map.put("allowSimultaneousConnections", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                ScheduledConnectionTaskInfo scheduledConnectionMethodInfo = (ScheduledConnectionTaskInfo) connectionTaskInfo;
                scheduledConnectionMethodInfo.allowSimultaneousConnections = ((ScheduledConnectionTask) connectionTask).isSimultaneousConnectionsAllowed();
            }
        });
        map.put("rescheduleRetryDelay", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                ScheduledConnectionTaskInfo scheduledConnectionMethodInfo = (ScheduledConnectionTaskInfo) connectionTaskInfo;
                TimeDuration rescheduleDelay = ((ScheduledConnectionTask) connectionTask).getRescheduleDelay();
                if (rescheduleDelay!=null) {
                    scheduledConnectionMethodInfo.rescheduleRetryDelay = new TimeDurationInfo(rescheduleDelay);
                }
            }
        });
        map.put("nextExecutionSpecs", (connectionTaskInfo, connectionTask, uriInfo) -> {
            if (ScheduledConnectionTask.class.isAssignableFrom(connectionTask.getClass())) {
                ScheduledConnectionTaskInfo scheduledConnectionMethodInfo = (ScheduledConnectionTaskInfo) connectionTaskInfo;
                NextExecutionSpecs nextExecutionSpecs = ((ScheduledConnectionTask) connectionTask).getNextExecutionSpecs();
                if (nextExecutionSpecs!=null) {
                    scheduledConnectionMethodInfo.nextExecutionSpecs = TemporalExpressionInfo.from(nextExecutionSpecs.getTemporalExpression());
                }
            }
        });
        return map;
    }

}
