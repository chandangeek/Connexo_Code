package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PropertyCopier;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
import com.elster.jupiter.rest.api.util.v1.hypermedia.SelectableFieldFactory;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.scheduling.model.ComSchedule;

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

public class ComScheduleInfoFactory extends SelectableFieldFactory<ComScheduleInfo, ComSchedule> {

    private final DeviceService deviceService;
    private final Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider;

    @Inject
    public ComScheduleInfoFactory(DeviceService deviceService, Provider<ComTaskInfoFactory> comTaskInfoFactoryProvider) {
        this.deviceService = deviceService;
        this.comTaskInfoFactoryProvider = comTaskInfoFactoryProvider;
    }

    public LinkInfo asLink(ComSchedule comSchedule, Relation relation, UriInfo uriInfo) {
        ComScheduleInfo info = new ComScheduleInfo();
        copySelectedFields(info,comSchedule,uriInfo, Arrays.asList("id","version"));
        info.link = link(comSchedule,relation,uriInfo);
        return info;
    }

    public List<LinkInfo> asLink(Collection<ComSchedule> comSchedules, Relation relation, UriInfo uriInfo) {
        return comSchedules.stream().map(ct-> asLink(ct, relation, uriInfo)).collect(toList());
    }

    private Link link(ComSchedule comSchedule, Relation relation, UriInfo uriInfo) {
        return Link.fromUriBuilder(getUriBuilder(uriInfo))
                .rel(relation.rel())
                .title("Communication schedule")
                .build(comSchedule.getId());
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
        map.put("version", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.version = comSchedule.getVersion());
        map.put("link", ((comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.link = link(comSchedule, Relation.REF_SELF, uriInfo)));
        map.put("name", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.name = comSchedule.getName());
        map.put("temporalExpression", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.temporalExpression = comSchedule.getTemporalExpression());
        map.put("plannedDate", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.plannedDate = comSchedule.getPlannedDate().orElse(null));
        map.put("isInUse", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.isInUse = deviceService.isLinkedToDevices(comSchedule));
        map.put("comTasks", ((comScheduleInfo, comSchedule, uriInfo) -> comTaskInfoFactoryProvider.get().asLink(comSchedule.getComTasks(), Relation.REF_RELATION, uriInfo)));
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
