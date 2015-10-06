package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static java.util.stream.Collectors.toList;

public class ComTaskExecutionInfoFactory extends SelectableFieldFactory<ComTaskExecutionInfo, ComTaskExecution> {

    public ComTaskExecutionInfo from(ComTaskExecution comTaskExecution, UriInfo uriInfo, Collection<String> fields) {
        ComTaskExecutionInfo info = new ComTaskExecutionInfo();
        copySelectedFields(info, comTaskExecution, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ComTaskExecutionInfo, ComTaskExecution>> buildFieldMap() {
        Map<String, PropertyCopier<ComTaskExecutionInfo, ComTaskExecution>> map = new HashMap<>();
        map.put("id", (comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.id = comTaskExecution.getId());
        map.put("link", ((comTaskExecutionInfo, comTaskExecution, uriInfo) ->
            comTaskExecutionInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(ComTaskExecutionResource.class).
                    path(ComTaskExecutionResource.class, "getComTaskExecution")).
                    rel(LinkInfo.REF_SELF).
                    title("Communication task execution").
                    build(comTaskExecution.getDevice().getmRID(), comTaskExecution.getId())
        ));
        map.put("device", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.device = new LinkInfo();
            comTaskExecutionInfo.device.id = comTaskExecution.getDevice().getId();
            comTaskExecutionInfo.device.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(DeviceResource.class).
                    path(DeviceResource.class, "getDevice")).
                    rel(LinkInfo.REF_PARENT).
                    title("Device").
                    build(comTaskExecution.getDevice().getmRID());
        }));
        map.put("comTasks", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {

            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                    .path(ComTaskResource.class)
                    .path(ComTaskResource.class, "getComTask");
            comTaskExecutionInfo.comTasks = comTaskExecution.getComTasks()
                    .stream()
                    .map(comTask -> {
                        LinkInfo linkInfo = new LinkInfo();
                        linkInfo.id = comTask.getId();
                        linkInfo.link = Link.fromUriBuilder(uriBuilder).rel(LinkInfo.REF_RELATION).title("Communication task").build(comTask.getId());
                        return linkInfo;
                    })
                    .collect(toList());
        }));
        map.put("nextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.nextExecution = comTaskExecution.getNextExecutionTimestamp()));
        map.put("plannedNextExecution", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.plannedNextExecution = comTaskExecution.getPlannedNextExecutionTimestamp()));
        map.put("priority", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.priority = comTaskExecution.getPlannedPriority()));
        map.put("schedule", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            if (comTaskExecution.usesSharedSchedule()) {
                UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                        .path(ComScheduleResource.class)
                        .path(ComScheduleResource.class, "getComSchedule");
                comTaskExecutionInfo.schedule = new LinkInfo();
                comTaskExecutionInfo.schedule.link = Link.fromUriBuilder(uriBuilder)
                        .rel(LinkInfo.REF_RELATION)
                        .title("Shared communication schedule")
                        .build(((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getId());
                comTaskExecutionInfo.schedule.id = ((ScheduledComTaskExecution) comTaskExecution).getComSchedule().getId();
            }
        }));
        map.put("type", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> {
            comTaskExecutionInfo.type = comTaskExecution.isScheduledManually()?
                    (comTaskExecution.isAdHoc()?
                            ComTaskExecutionType.AdHoc:
                            ComTaskExecutionType.ManualSchedule):
                        ComTaskExecutionType.SharedSchedule;
        }));
        map.put("lastCommunicationStart", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.lastCommunicationStart = comTaskExecution.getLastExecutionStartTimestamp()));
        map.put("lastSuccessfulCompletion", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.lastSuccessfulCompletion = comTaskExecution.getLastSuccessfulCompletionTimestamp()));
        map.put("isOnHold", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.isOnHold = comTaskExecution.isOnHold()));
        map.put("status", ((comTaskExecutionInfo, comTaskExecution, uriInfo) -> comTaskExecutionInfo.status = comTaskExecution.getStatus()));

        return map;
    }
}
