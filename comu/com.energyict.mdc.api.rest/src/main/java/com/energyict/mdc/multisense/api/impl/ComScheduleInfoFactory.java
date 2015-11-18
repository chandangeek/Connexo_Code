package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.PropertyCopier;
import com.energyict.mdc.multisense.api.impl.utils.SelectableFieldFactory;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ComScheduleInfoFactory extends SelectableFieldFactory<ComScheduleInfo, ComSchedule> {

    private final DeviceService deviceService;
    private final ComTaskInfoFactory comTaskInfoFactory;

    @Inject
    public ComScheduleInfoFactory(DeviceService deviceService, ComTaskInfoFactory comTaskInfoFactory) {
        this.deviceService = deviceService;
        this.comTaskInfoFactory = comTaskInfoFactory;
    }

    public LinkInfo asLink(ComSchedule comSchedule, Relation relation, UriInfo uriInfo) {
        return asLink(comSchedule, relation, getUriBuilder(uriInfo));
    }

    public List<LinkInfo> asLink(Collection<ComSchedule> comSchedules, Relation relation, UriInfo uriInfo) {
        UriBuilder uriBuilder = getUriBuilder(uriInfo);
        return comSchedules.stream().map(ct-> asLink(ct, relation, uriBuilder)).collect(toList());
    }

    private LinkInfo asLink(ComSchedule comSchedule, Relation relation, UriBuilder uriBuilder) {
        LinkInfo info = new LinkInfo();
        info.id = comSchedule.getId();
        info.link = Link.fromUriBuilder(uriBuilder)
                .rel(relation.rel())
                .title("Communication schedule")
                .build(comSchedule.getId());
        return info;
    }

    public ComScheduleInfo from(ComSchedule comSchedule, UriInfo uriInfo, Collection<String> fields) {
        ComScheduleInfo info = new ComScheduleInfo();
        copySelectedFields(info, comSchedule, uriInfo, fields);
        return info;
    }

    @Override
    protected Map<String, PropertyCopier<ComScheduleInfo, ComSchedule>> buildFieldMap() {
        Map<String, PropertyCopier<ComScheduleInfo, ComSchedule>> map = new HashMap<>();
        map.put("id", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.id = comSchedule.getId());
        map.put("link", ((comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.link = this.asLink(comSchedule, Relation.REF_SELF, uriInfo).link));
        map.put("name", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.name = comSchedule.getName());
        map.put("temporalExpression", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.temporalExpression = comSchedule.getTemporalExpression());
        map.put("plannedDate", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.plannedDate = comSchedule.getPlannedDate().orElse(null));
        map.put("isInUse", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.isInUse = deviceService.isLinkedToDevices(comSchedule));
        map.put("comTasks", ((comScheduleInfo, comSchedule, uriInfo) -> comTaskInfoFactory.asLink(comSchedule.getComTasks(), Relation.REF_RELATION, uriInfo)));
        map.put("startDate", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.startDate = comSchedule.getStartDate());
        map.put("mRID", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.mRID = comSchedule.getmRID().orElse(null));
        return map;
    }

    private UriBuilder getUriBuilder(UriInfo uriInfo) {
        return uriInfo.getBaseUriBuilder().
                path(ComScheduleResource.class).
                path(ComScheduleResource.class, "getComSchedule");
    }

}
