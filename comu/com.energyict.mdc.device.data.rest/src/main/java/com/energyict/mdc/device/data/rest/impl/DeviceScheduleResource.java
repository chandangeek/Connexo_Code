package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.JsonQueryFilter;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class DeviceScheduleResource {

    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceScheduleResource(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
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
                    boolean comTaskExecutionExists = false;
                    for (ComTaskExecution comTaskExecution : device.getComTaskExecutions()) {
                        if(comTaskExecution.isAdHoc() && comTaskExecution.getComTasks().get(0).getId()==comTaskEnablement.getComTask().getId()){

                            ((ManuallyScheduledComTaskExecution)comTaskExecution).getUpdater().scheduleAccordingTo(schedulingInfo.schedule.asTemporalExpression()).update();
                            comTaskExecutionExists = true;
                        }
                    }
                    if(!comTaskExecutionExists) {
                        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> builder = device.newManuallyScheduledComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orElse(null), schedulingInfo.schedule.asTemporalExpression());
                        if(comTaskEnablement.hasPartialConnectionTask()){
                            for (ConnectionTask<?, ?> connectionTask : device.getConnectionTasks()) {
                                if(connectionTask.getPartialConnectionTask().getId()==comTaskEnablement.getPartialConnectionTask().get().getId()){
                                    builder.connectionTask(connectionTask);
                                }
                            }
                        }
                        builder.add();
                    }
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
                        ManuallyScheduledComTaskExecution comTaskExecution = device.newAdHocComTaskExecution(comTaskEnablement, comTaskEnablement.getProtocolDialectConfigurationProperties().orElse(null)).add();
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
