/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
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

    /**
     * Returns the identified communication task execution.
     * If the <i>fields</i>-query parameters was provided, only the requested fields will be returned. If no such query
     * parameter was provided, all existing fields will be returned. Note that empty fields, that is, fields without value,
     * will not be included in the response.
     *
     * @summary Fetch single communication task execution
     *
     * @param mRID The device's mRID
     * @param comTaskExecutionId Id of the communication task execution
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     *
     * @return Communication task execution
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskExecutionId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ComTaskExecutionInfo getComTaskExecution(@PathParam("mrid") String mRID, @PathParam("comTaskExecutionId") long comTaskExecutionId,
                                                    @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        return deviceService.findDeviceByMrid(mRID)
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                 .getComTaskExecutions().stream()
                 .filter(comTaskExecution -> comTaskExecution.getId()==comTaskExecutionId)
                 .findFirst()
                 .map(ct -> comTaskExecutionInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                 .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION));
    }

    /**
     * Fetch all existing communication task executions of a certain device
     *
     * @summary Fetch a device's communication task executions
     *
     * @param mRID mRID of device for which executions will be retrieved
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     *
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ComTaskExecutionInfo> getComTaskExecutions(@PathParam("mrid") String mRID, @BeanParam FieldSelection fieldSelection,
                                                                    @Context UriInfo uriInfo, @BeanParam JsonQueryParameters queryParameters) {
        List<ComTaskExecutionInfo> infoList = deviceService.findDeviceByMrid(mRID)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getComTaskExecutions().stream()
                .map(cte -> comTaskExecutionInfoFactory.from(cte, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComTaskExecutionResource.class)
                .resolveTemplate("mrid", mRID);
        return PagedInfoList.from(infoList, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Create a new communication task execution for a device
     *
     * @summary Create communication task execution
     *
     * @param mrid mRID of device for which execution will be created
     * @param comTaskExecutionInfo Payload describing to-be-created communication task execution
     * @param uriInfo uriInfo
     *
     * @return no content
     * @responseheader location href to newly created device
     */
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

    /**
     * Updates an existing communication task execution.
     *
     * @summary Updates an existing communication task execution
     *
     * @param mrid mRID of device for which execution will be updated
     * @param comTaskExecutionId Identifier of the device's communication task execution
     * @param comTaskExecutionInfo Contents for updated communication task execution
     * @param uriInfo uriInfo
     *
     * @return URI to updated communication task execution
     */
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

    /**
     * Delete an existing new communication task execution for a device
     *
     * @summary Delete communication task execution
     *
     * @param mrid mRID of device whose communication task execution needs to be deleted
     * @param comTaskExecutionid The ID of the communication task execution that needs to be deleted
     *
     * @return No content
     */
    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskExecutionId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public Response deleteComTaskExecution(@PathParam("mrid") String mrid, @PathParam("comTaskExecutionId") long comTaskExecutionid) {
        Device device = deviceService.findDeviceByMrid(mrid)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        ComTaskExecution comTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getId() == comTaskExecutionid)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_EXECUTION));
        device.removeComTaskExecution(comTaskExecution);

        return Response.noContent().build();
    }


    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br> "actions",
     * <br> "batch"
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name,batch</i>
     * <br> The call above will return only the requested fields of the entity. In the absence of a field list, all fields
     * will be returned. If IDs are required in the URL for parent entities, then will be ignored when using the PROPFIND method.
     *
     * @summary List the fields available on this type of entity
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return comTaskExecutionInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
