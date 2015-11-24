package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/comtaskexecutions")
public class ComTaskExecutionResource {

    private final ComTaskExecutionInfoFactory comTaskExecutionInfoFactory;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final CommunicationTaskService communicationTaskService;

    @Inject
    public ComTaskExecutionResource(DeviceService deviceService, ComTaskExecutionInfoFactory comTaskExecutionInfoFactory, ExceptionFactory exceptionFactory, CommunicationTaskService communicationTaskService) {
        this.deviceService = deviceService;
        this.comTaskExecutionInfoFactory = comTaskExecutionInfoFactory;
        this.exceptionFactory = exceptionFactory;
        this.communicationTaskService = communicationTaskService;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskExecutionId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ComTaskExecutionInfo getComTaskExecution(@PathParam("mrid") String mRID, @PathParam("comTaskExecutionId") long comTaskExecutionId, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
         return deviceService.findByUniqueMrid(mRID)
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                 .getComTaskExecutions().stream()
                 .filter(comTaskExecution -> comTaskExecution.getId()==comTaskExecutionId)
                 .findFirst()
                 .map(ct -> comTaskExecutionInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION));
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ComTaskExecutionInfo> getComTaskExecutions(@PathParam("mrid") String mRID, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComTaskExecutionInfo> infoList = deviceService.findByUniqueMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getComTaskExecutions().stream()
                .map(cte -> comTaskExecutionInfoFactory.from(cte, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComTaskExecutionResource.class)
                .resolveTemplate("mrid", mRID);
        return PagedInfoList.from(infoList, queryParameters, uriBuilder, uriInfo);
    }

    @POST @Transactional
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response createComTaskExecution(@PathParam("mrid") String mrid, ComTaskExecutionInfo comTaskExecutionInfo, @Context UriInfo uriInfo) {
        if (comTaskExecutionInfo.device == null || comTaskExecutionInfo.device.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "device");
        }

        Device device = deviceService.findAndLockDeviceBymRIDAndVersion(mrid, comTaskExecutionInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        ComTaskExecution comTaskExecution = comTaskExecutionInfo.type.createComTaskExecution(comTaskExecutionInfoFactory, comTaskExecutionInfo, device);
        URI uri = uriInfo.getBaseUriBuilder()
                .path(ComTaskExecutionResource.class)
                .path(ComTaskExecutionResource.class, "getComTaskExecution")
                .resolveTemplate("mrid", mrid)
                .resolveTemplate("comTaskExecutionId", comTaskExecution.getId())
                .build();
        return Response.created(uri).build();

    }

    @PUT @Transactional
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{comTaskExecutionId}")
    public Response updateComTaskExecution(@PathParam("mrid") String mrid, @PathParam("comTaskExecutionId") long comTaskExecutionId,
                                           ComTaskExecutionInfo comTaskExecutionInfo, @Context UriInfo uriInfo) {
        if (comTaskExecutionInfo.device == null || comTaskExecutionInfo.device.version == null) {
            throw exceptionFactory.newException(Response.Status.BAD_REQUEST, MessageSeeds.VERSION_MISSING, "device");
        }
        deviceService.findAndLockDeviceBymRIDAndVersion(mrid, comTaskExecutionInfo.device.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        ComTaskExecution comTaskExecution = communicationTaskService.findAndLockComTaskExecutionByIdAndVersion(comTaskExecutionId, comTaskExecutionInfo.version)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION));

        if (!comTaskExecution.getDevice().getmRID().equals(mrid)) {
            throw exceptionFactory.newException(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION);
        }

        ComTaskExecution updatedComTaskExecution = comTaskExecutionInfo.type.updateComTaskExecution(comTaskExecutionInfoFactory, comTaskExecutionInfo, comTaskExecution);
        URI uri = uriInfo.getBaseUriBuilder()
                .path(ComTaskExecutionResource.class)
                .path(ComTaskExecutionResource.class, "getComTaskExecution")
                .resolveTemplate("mrid", mrid)
                .resolveTemplate("comTaskExecutionId", updatedComTaskExecution.getId())
                .build();
        return Response.ok(uri).build();

    }

    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskExecutionId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response deleteComTaskExecution(@PathParam("mrid") String mrid, @PathParam("comTaskExecutionId") long comTaskExecutionid) {
        Device device = deviceService.findByUniqueMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        ComTaskExecution comTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getId() == comTaskExecutionid)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION));
        device.removeComTaskExecution(comTaskExecution);

        return Response.noContent().build();
    }


    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return comTaskExecutionInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
