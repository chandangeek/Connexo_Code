package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.scheduling.NextExecutionSpecs;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/15/15.
 */
public class PartialConnectionTaskInfoFactory extends SelectableFieldFactory<PartialConnectionTaskInfo, PartialConnectionTask> {

    private final MdcPropertyUtils mdcPropertyUtils;

    @Inject
    public PartialConnectionTaskInfoFactory(MdcPropertyUtils mdcPropertyUtils) {
        this.mdcPropertyUtils = mdcPropertyUtils;
    }

    public LinkInfo asLink(PartialConnectionTask partialConnectionTask, Relation relation, UriInfo uriInfo) {
        return asLink(partialConnectionTask, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<PartialConnectionTask> partialConnectionTasks, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return partialConnectionTasks.stream().map(i-> asLink(i, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(PartialConnectionTask partialConnectionTask, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = partialConnectionTask.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Partial connection task")
                .build(partialConnectionTask.getConfiguration().getDeviceType().getId(),
                        partialConnectionTask.getConfiguration().getId(),
                        partialConnectionTask.getId());
        return info;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder()
                .path(PartialConnectionTaskResource.class)
                .path(PartialConnectionTaskResource.class, "getPartialConnectionTask");
    }

    public PartialConnectionTaskInfo from(PartialConnectionTask partialConnectionTask, UriInfo uriInfo, Collection<String> fields) {
        PartialConnectionTaskInfo info = new PartialConnectionTaskInfo();
        copySelectedFields(info, partialConnectionTask, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<PartialConnectionTaskInfo, PartialConnectionTask>> buildFieldMap() {
        HashMap<String, PropertyCopier<PartialConnectionTaskInfo, PartialConnectionTask>> map = new HashMap<>();
        map.put("id",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.id = partialConnectionTask.getId());
        map.put("name",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.name = partialConnectionTask.getName());
        map.put("direction",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.direction = ConnectionTaskType.from(partialConnectionTask));
        map.put("link",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(PartialConnectionTaskResource.class).
                    path(PartialConnectionTaskResource.class, "getPartialConnectionTask");
            partialConnectionTaskInfo.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_SELF.rel()).build(partialConnectionTask.getConfiguration().getDeviceType().getId(), partialConnectionTask.getConfiguration().getId(), partialConnectionTask.getId());
        });
        map.put("connectionType", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.connectionType = partialConnectionTask.getPluggableClass().getName());
        map.put("comPortPool", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            partialConnectionTaskInfo.comPortPool = new LinkInfo();
            partialConnectionTaskInfo.comPortPool.id = partialConnectionTask.getComPortPool().getId();
            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().
                    path(ComPortPoolResource.class).
                    path(ComPortPoolResource.class, "getComPortPool");
            partialConnectionTaskInfo.comPortPool.link = Link.fromUriBuilder(uriBuilder).rel(Relation.REF_RELATION.rel()).build(partialConnectionTaskInfo.comPortPool.id);
        });
        map.put("isDefault", (partialConnectionTaskInfo, partialConnectionTask, uriInfo)-> partialConnectionTaskInfo.isDefault = partialConnectionTask.isDefault());
        map.put("properties", (partialConnectionTaskInfo, partialConnectionTask, uriInfo)-> partialConnectionTaskInfo.properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(partialConnectionTask.getConnectionType().getPropertySpecs(), partialConnectionTask.getTypedProperties()));
        map.put("comWindow", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
                ComWindow communicationWindow = ((PartialScheduledConnectionTask) partialConnectionTask).getCommunicationWindow();
                if (communicationWindow!=null) {
                    partialConnectionTaskInfo.comWindow = new ComWindowInfo();
                    partialConnectionTaskInfo.comWindow.start = communicationWindow.getStart()!=null?communicationWindow.getStart().getMillis():null;
                    partialConnectionTaskInfo.comWindow.end = communicationWindow.getEnd()!=null?communicationWindow.getEnd().getMillis():null;
                }
            }
        });
        map.put("connectionStrategy", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
                partialConnectionTaskInfo.connectionStrategy = ((PartialScheduledConnectionTask) partialConnectionTask).getConnectionStrategy();
            }
        });
        map.put("allowSimultaneousConnections", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
                partialConnectionTaskInfo.allowSimultaneousConnections = ((PartialScheduledConnectionTask) partialConnectionTask).isSimultaneousConnectionsAllowed();
            }
        });
        map.put("rescheduleRetryDelay", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
                TimeDuration rescheduleDelay = ((PartialScheduledConnectionTask) partialConnectionTask).getRescheduleDelay();
                if (rescheduleDelay!=null) {
                    partialConnectionTaskInfo.rescheduleRetryDelay = new TimeDurationInfo(rescheduleDelay);
                }
            }
        });
        map.put("nextExecutionSpecs", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
                NextExecutionSpecs nextExecutionSpecs = ((PartialScheduledConnectionTask) partialConnectionTask).getNextExecutionSpecs();
                if (nextExecutionSpecs!=null) {
                    partialConnectionTaskInfo.nextExecutionSpecs = TemporalExpressionInfo.from(nextExecutionSpecs.getTemporalExpression());
                }
            }
        });
        
        return map;
    }
}
