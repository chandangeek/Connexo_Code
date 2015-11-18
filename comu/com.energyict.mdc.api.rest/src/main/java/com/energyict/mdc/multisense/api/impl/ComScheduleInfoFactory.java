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
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class ComScheduleInfoFactory extends SelectableFieldFactory<ComScheduleInfo, ComSchedule> {

    private final DeviceService deviceService;

    @Inject
    public ComScheduleInfoFactory(DeviceService deviceService) {
        this.deviceService = deviceService;
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
        map.put("link", ((comScheduleInfo, comSchedule, uriInfo) ->
            comScheduleInfo.link = Link.fromUriBuilder(uriInfo.
                    getBaseUriBuilder().
                    path(ComScheduleResource.class).
                    path(ComScheduleResource.class, "getComSchedule")).
                    rel(LinkInfo.REF_SELF).
                    title("Communication schedule").
                    build(comSchedule.getId())
        ));

        map.put("id", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.id = comSchedule.getId());
        map.put("name", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.name = comSchedule.getName());
        map.put("temporalExpression", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.temporalExpression = comSchedule.getTemporalExpression());
        map.put("plannedDate", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.plannedDate = comSchedule.getPlannedDate().orElse(null));
        map.put("isInUse", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.isInUse = deviceService.isLinkedToDevices(comSchedule));
        map.put("comTasks", ((comScheduleInfo, comSchedule, uriInfo) -> {
            UriBuilder uirBuilder = uriInfo
                    .getBaseUriBuilder()
                    .path(ComTaskResource.class)
                    .path(ComTaskResource.class, "getComTask");
            comSchedule.getComTasks().stream().map(ct -> {
                        LinkInfo info = new LinkInfo();
                        info.id = ct.getId();
                        info.link = Link.fromUriBuilder(uirBuilder)
                        .rel(LinkInfo.REF_RELATION)
                        .title("Scheduled communication task")
                        .build(ct.getId());

                return info;
                    }).collect(toList());
        }));

        map.put("startDate", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.startDate = comSchedule.getStartDate());
        map.put("mRID", (comScheduleInfo, comSchedule, uriInfo) -> comScheduleInfo.mRID = comSchedule.getmRID().orElse(null));
        return map;
    }
}
