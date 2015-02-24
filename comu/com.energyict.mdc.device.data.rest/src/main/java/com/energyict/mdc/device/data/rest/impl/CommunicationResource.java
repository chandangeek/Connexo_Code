package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommunicationResource {

    private final ResourceHelper resourceHelper;
    private final DeviceComTaskExecutionInfoFactory deviceComTaskExecutionInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public CommunicationResource(ResourceHelper resourceHelper, DeviceComTaskExecutionInfoFactory deviceComTaskExecutionInfoFactory, ExceptionFactory exceptionFactory) {
        this.resourceHelper = resourceHelper;
        this.deviceComTaskExecutionInfoFactory = deviceComTaskExecutionInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE, Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public PagedInfoList getCommunications(@PathParam("mRID") String mRID, @BeanParam QueryParameters queryParameters) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        List<DeviceComTaskExecutionInfo> infos = device.getComTaskExecutions().stream()
                .map((cte) -> deviceComTaskExecutionInfoFactory.from(cte, cte.getLastSession()))
                .collect(Collectors.toList());
        return PagedInfoList.fromPagedList("communications", infos, queryParameters);
    }

    @PUT
    @Path("/{comTaskExecId}/run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunication(@PathParam("mRID") String mRID, @PathParam("comTaskExecId") long comTaskExecId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ComTaskExecution comTaskExecution = findComTaskExecutionOrThrowException(device, comTaskExecId);
        comTaskExecution.scheduleNow();
        return Response.ok(deviceComTaskExecutionInfoFactory.from(comTaskExecution, comTaskExecution.getLastSession())).build();
    }

    @PUT
    @Path("/{comTaskExecId}/runnow")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response runCommunicationNow(@PathParam("mRID") String mRID, @PathParam("comTaskExecId") long comTaskExecId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ComTaskExecution comTaskExecution = findComTaskExecutionOrThrowException(device, comTaskExecId);
        comTaskExecution.runNow();
        return Response.ok(deviceComTaskExecutionInfoFactory.from(comTaskExecution, comTaskExecution.getLastSession())).build();
    }

    @PUT
    @Path("/{comTaskExecId}/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activate(@PathParam("mRID") String mRID, @PathParam("comTaskExecId") long comTaskExecId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ComTaskExecution comTaskExecution = findComTaskExecutionOrThrowException(device, comTaskExecId);
        activateComTaskExecution(comTaskExecution);
        return Response.ok(deviceComTaskExecutionInfoFactory.from(comTaskExecution, comTaskExecution.getLastSession())).build();
    }

    @PUT
    @Path("/{comTaskExecId}/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response deactivate(@PathParam("mRID") String mRID, @PathParam("comTaskExecId") long comTaskExecId) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ComTaskExecution comTaskExecution = findComTaskExecutionOrThrowException(device, comTaskExecId);
        deactivateComTaskExecution(comTaskExecution);
        return Response.ok(deviceComTaskExecutionInfoFactory.from(comTaskExecution, comTaskExecution.getLastSession())).build();
    }

    @PUT
    @Path("/{comTaskExecId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateDeactiveCommunication(@PathParam("mRID") String mRID, @PathParam("comTaskExecId") long comTaskExecId, DeviceComTaskExecutionInfo info) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        ComTaskExecution comTaskExecution = findComTaskExecutionOrThrowException(device, comTaskExecId);
        if (info.isOnHold) {
            deactivateComTaskExecution(comTaskExecution);
        } else {
            activateComTaskExecution(comTaskExecution);
        }
        return Response.ok(deviceComTaskExecutionInfoFactory.from(comTaskExecution, comTaskExecution.getLastSession())).build();
    }

    @PUT
    @Path("/activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response activateAllCommunication(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        device.getComTaskExecutions().stream().forEach(cte -> activateComTaskExecution(cte));
        return Response.ok().build();
    }

    @PUT
    @Path("/deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.OPERATE_DEVICE_COMMUNICATION, Privileges.ADMINISTRATE_DEVICE_COMMUNICATION})
    public Response deactivateAllCommunication(@PathParam("mRID") String mRID) {
        Device device = resourceHelper.findDeviceByMrIdOrThrowException(mRID);
        device.getComTaskExecutions().stream().forEach(cte -> deactivateComTaskExecution(cte));
        return Response.ok().build();
    }

    private ComTaskExecution findComTaskExecutionOrThrowException(Device device, long comTaskExecId) {
        Optional<ComTaskExecution> comTaskExecution = device.getComTaskExecutions().stream().filter(cte -> cte.getId() == comTaskExecId).findAny();
        return comTaskExecution.orElseThrow(() -> exceptionFactory.newException(MessageSeeds.NO_SUCH_COMMUNICATION, comTaskExecId, device.getmRID()));
    }

    private void activateComTaskExecution(ComTaskExecution comTaskExecution) {
        if (comTaskExecution.isOnHold()) {
            comTaskExecution.updateNextExecutionTimestamp();
        }
    }

    private void deactivateComTaskExecution(ComTaskExecution comTaskExecution) {
        if (!comTaskExecution.isOnHold()) {
            comTaskExecution.putOnHold();
        }
    }
}
