package com.energyict.mdc.scheduling.rest.impl;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/schedules")
public class SchedulingResource {

    private final SchedulingService schedulingService;

    @Inject
    public SchedulingResource(SchedulingService schedulingService) {
        this.schedulingService = schedulingService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getSchedules(@BeanParam QueryParameters queryParameters) {
        List<ComSchedule> comSchedules = schedulingService.findAllSchedules().from(queryParameters).find();
        List<ComScheduleInfo> comScheduleInfos = new ArrayList<>();
        for (ComSchedule comSchedule : comSchedules) {
            comScheduleInfos.add(ComScheduleInfo.from(comSchedule));
        }

        return PagedInfoList.asJson("schedules", comScheduleInfos, queryParameters);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSchedule(ComScheduleInfo comScheduleInfo) {
        ComSchedule comSchedule = schedulingService.newComSchedule(comScheduleInfo.name, comScheduleInfo.nextExecutionSpecs.asTemporalExpression());
        comSchedule.save();
        return Response.status(Response.Status.CREATED).entity(ComScheduleInfo.from(comSchedule)).build();
    }
}
