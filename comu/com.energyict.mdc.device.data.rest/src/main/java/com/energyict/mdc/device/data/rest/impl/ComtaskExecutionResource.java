package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.configuration.rest.impl.ComTaskEnablementComparator;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.AdHocComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class ComtaskExecutionResource {

    private final ResourceHelper resourceHelper;
    private final ExceptionFactory exceptionFactory;
    private final Thesaurus thesaurus;

    @Inject
    public ComtaskExecutionResource(ResourceHelper resourceHelper, ExceptionFactory exceptionFactory, Thesaurus thesaurus) {
        this.resourceHelper = resourceHelper;
        this.exceptionFactory = exceptionFactory;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllComTaskExecutions(@PathParam("mRID") String mrid, @BeanParam QueryParameters queryParameters, @BeanParam JsonQueryFilter queryFilter){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements();
        List<DeviceSchedulesInfo> deviceSchedulesInfos = DeviceSchedulesInfo.from(comTaskExecutions,comTaskEnablements);
        return Response.ok(PagedInfoList.asJson("schedules", deviceSchedulesInfos,queryParameters)).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createComTaskExecution(@PathParam("mRID") String mrid,SchedulingInfo schedulingInfo){
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        for(ComTaskEnablement comTaskEnablement:deviceConfiguration.getComTaskEnablements()){
            if(comTaskEnablement.getComTask().getId()==schedulingInfo.id){
                if(schedulingInfo.schedule!=null){
                    device.newManuallyScheduledComTaskExecution(comTaskEnablement,comTaskEnablement.getProtocolDialectConfigurationProperties().orNull(),schedulingInfo.schedule.asTemporalExpression()).add();
                    device.save();
                } else {
                    boolean comTaskExecutionExists = false;
                    for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
                        if(comTaskExecution.isAdHoc() && comTaskExecution.getComTasks().get(0).getId()==comTaskEnablement.getComTask().getId()){
                            comTaskExecution.scheduleNow();
                            comTaskExecutionExists = true;
                        }
                    }
                    if(!comTaskExecutionExists){
                        AdHocComTaskExecution comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orNull()).add();
                        device.save();
                        comTaskExecution.scheduleNow();
                    }
                }
            }
        }
       return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateComTaskExecution(@PathParam("mRID") String mrid,SchedulingInfo schedulingInfo) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mrid);
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions();
        for(ComTaskExecution comTaskExecution:comTaskExecutions){
            if(comTaskExecution.getId()==schedulingInfo.id && comTaskExecution instanceof ManuallyScheduledComTaskExecution) {
                if(schedulingInfo.schedule == null){
                    device.removeComTaskExecution(comTaskExecution);
                } else {
                    ((ManuallyScheduledComTaskExecution)comTaskExecution).getUpdater().scheduleAccordingTo(schedulingInfo.schedule.asTemporalExpression()).update();
                }
            }
        }
        return Response.status(Response.Status.CREATED).build();
    }
}
