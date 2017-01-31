/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.rest.TimeDurationInfo;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
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
 * Created by bvn on 7/15/15.
 */
public class PartialConnectionTaskInfoFactory extends SelectableFieldFactory<PartialConnectionTaskInfo, PartialConnectionTask> {

    private final MdcPropertyUtils mdcPropertyUtils;
    private final Provider<ComPortPoolInfoFactory> comPortPoolInfoFactoryProvider;

    @Inject
    public PartialConnectionTaskInfoFactory(MdcPropertyUtils mdcPropertyUtils, Provider<ComPortPoolInfoFactory> comPortPoolInfoFactory) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.comPortPoolInfoFactoryProvider = comPortPoolInfoFactory;
    }

    public LinkInfo asLink(PartialConnectionTask partialConnectionTask, Relation relation, UriInfo uriInfo) {
        PartialConnectionTaskInfo info = new PartialConnectionTaskInfo();
        copySelectedFields(info,partialConnectionTask,uriInfo, Arrays.asList("id","version"));
        info.link = link(partialConnectionTask,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<PartialConnectionTask> partialConnectionTasks, Relation relation, UriInfo uriInfo) {
        return partialConnectionTasks.stream().map(i-> asLink(i, relation, uriInfo)).collect(toList());
    }

    private Link link(PartialConnectionTask partialConnectionTask, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Partial connection task")
                .build(partialConnectionTask.getConfiguration().getDeviceType().getId(),
                        partialConnectionTask.getConfiguration().getId(),
                        partialConnectionTask.getId());
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
        map.put("version",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.version = partialConnectionTask.getVersion());
        map.put("name",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.name = partialConnectionTask.getName());
        map.put("direction",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.direction = ConnectionTaskType.from(partialConnectionTask));
        map.put("link",(partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.link = link(partialConnectionTask, Relation.REF_SELF, uriInfo));
        map.put("connectionType", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> partialConnectionTaskInfo.connectionType = partialConnectionTask.getPluggableClass().getName());
        map.put("comPortPool", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) ->
                partialConnectionTaskInfo.comPortPool = comPortPoolInfoFactoryProvider.get().asLink(partialConnectionTask.getComPortPool(), Relation.REF_RELATION, uriInfo));
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
        map.put("numberOfSimultaneousConnections", (partialConnectionTaskInfo, partialConnectionTask, uriInfo) -> {
            if (PartialScheduledConnectionTask.class.isAssignableFrom(partialConnectionTask.getClass())) {
                partialConnectionTaskInfo.numberOfSimultaneousConnections = ((PartialScheduledConnectionTask) partialConnectionTask).getNumberOfSimultaneousConnections();
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
