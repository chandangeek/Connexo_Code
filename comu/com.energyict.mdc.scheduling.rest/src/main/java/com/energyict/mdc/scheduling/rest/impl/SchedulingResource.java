package com.energyict.mdc.scheduling.rest.impl;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.UtcInstant;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/schedules")
public class SchedulingResource {

    private final SchedulingService schedulingService;
    private final DeviceDataService deviceDataService;
    private final Clock clock;

    @Inject
    public SchedulingResource(SchedulingService schedulingService, DeviceDataService deviceDataService, Clock clock) {
        this.schedulingService = schedulingService;
        this.deviceDataService = deviceDataService;
        this.clock = clock;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getSchedules(@BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter) {
        Calendar calendar = Calendar.getInstance(clock.getTimeZone());
        List<ComSchedule> comSchedules = schedulingService.findAllSchedules(calendar).from(queryParameters).find();
        List<ComScheduleInfo> comScheduleInfos = new ArrayList<>();
        for (ComSchedule comSchedule : comSchedules) {
            comScheduleInfos.add(ComScheduleInfo.from(comSchedule, isInUse(comSchedule)));
        }

        return PagedInfoList.asJson("schedules", comScheduleInfos, queryParameters);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComScheduleInfo getSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        return ComScheduleInfo.from(comSchedule, isInUse(comSchedule));
    }

    private ComSchedule findComScheduleOrThrowException(long id) {
        ComSchedule comSchedule = schedulingService.findSchedule(id);
        if (comSchedule==null) {
            throw new WebApplicationException("No such schedule", Response.Status.NOT_FOUND);
        }
        return comSchedule;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSchedule(ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleInfo.name, comScheduleInfo.temporalExpression.asTemporalExpression(),
                comScheduleInfo.startDate==null?null:new UtcInstant(comScheduleInfo.startDate));
        comSchedule.save();
        return Response.status(Response.Status.CREATED).entity(ComScheduleInfo.from(comSchedule, isInUse(comSchedule))).build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSchedules(@PathParam("id") long id) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        comSchedule.delete();
        return Response.noContent().build();
    }

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComScheduleInfo updateSchedules(@PathParam("id") long id, ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = findComScheduleOrThrowException(id);
        comSchedule.setName(comScheduleInfo.name);
        comSchedule.setTemporalExpression(comScheduleInfo.temporalExpression.asTemporalExpression());
        comSchedule.setSchedulingStatus(comScheduleInfo.schedulingStatus);
        comSchedule.setStartDate(comScheduleInfo.startDate==null?null:new UtcInstant(comScheduleInfo.startDate));
        comSchedule.save();
        return ComScheduleInfo.from(findComScheduleOrThrowException(id), isInUse(comSchedule));
    }


    private boolean isInUse(ComSchedule comSchedule) {
        return this.deviceDataService.isLinkedToDevices(comSchedule);
    }

}
