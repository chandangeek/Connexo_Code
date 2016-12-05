package com.energyict.mdc.device.alarms.rest.resource;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.device.DeviceInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfo;
import com.energyict.mdc.device.alarms.rest.response.DeviceAlarmInfoFactory;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;


@Path("/alarms")
public class DeviceAlarmResource{

    private final DeviceAlarmService deviceAlarmService;
    private final DeviceAlarmInfoFactory deviceAlarmInfoFactory;


    @Inject
    public DeviceAlarmResource(DeviceAlarmService deviceAlarmService, DeviceAlarmInfoFactory deviceAlarmInfoFactory){
        this.deviceAlarmService = deviceAlarmService;
        this.deviceAlarmInfoFactory = deviceAlarmInfoFactory;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public PagedInfoList getAllDeviceAlarms(@BeanParam StandardParametersBean params, @BeanParam JsonQueryParameters queryParams, @BeanParam JsonQueryFilter filter){
        validateMandatory(params, START, LIMIT);
        Finder<? extends DeviceAlarm> finder = deviceAlarmService.findAlarms(new DeviceAlarmFilter()); //FixMe implement filter;
        addSorting(finder, params);
        if (queryParams.getStart().isPresent() && queryParams.getLimit().isPresent()) {
            finder.paged(queryParams.getStart().get(), queryParams.getLimit().get());
        }
        List<? extends DeviceAlarm> deviceAlarms = finder.find();
        List<DeviceAlarmInfo<?>> deviceAlarmInfos = deviceAlarms.stream()
                .map(alarm -> deviceAlarmInfoFactory.asInfo(alarm, DeviceInfo.class))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("data", deviceAlarmInfos, queryParams);
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAlarmById(@PathParam("id") long id) {
        Optional<? extends DeviceAlarm> deviceAlarm = deviceAlarmService.findAlarm(id);
        return deviceAlarm.map(i -> Response.ok().entity(deviceAlarmInfoFactory.asInfo(i, DeviceInfo.class)).build())
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void validateMandatory(StandardParametersBean params, String... mandatoryParameters) {
        if (mandatoryParameters != null) {
            Arrays.asList(mandatoryParameters).stream().map(params::getFirst).forEach(param -> {
                if(param == null){
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            });
        }
    }

    private Finder<? extends DeviceAlarm> addSorting(Finder<? extends DeviceAlarm> finder, StandardParametersBean parameters) {
        Order[] orders = parameters.getOrder("");
        for (Order order : orders) {
            finder.sorted(order.getName(), order.ascending());
        }
        return finder;
    }
}
