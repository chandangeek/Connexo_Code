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
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 10/8/15.
 */
@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/comtaskenablements")
public class ComTaskEnablementResource {

    private final ComTaskEnablementInfoFactory comTaskEnablementInfoFactory;
    private final DeviceConfigurationService deviceConfigurationService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public ComTaskEnablementResource(ComTaskEnablementInfoFactory comTaskEnablementInfoFactory, DeviceConfigurationService deviceConfigurationService, ExceptionFactory exceptionFactory) {
        this.comTaskEnablementInfoFactory = comTaskEnablementInfoFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Enables the execution of a ComTask against devices of a device configuration and specifies the security
     * requirements for that execution.
     * <br>
     * In addition, specifies preferred scheduling (e.g. every day or every week)
     * and preferred PartialConnectionTask or if the execution of the ComTask
     * should use the default ConnectionTask.
     *
     * @summary Fetch a set of communication task enablements
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @param queryParameters queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ComTaskEnablementInfo> getComTaskEnablements(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceConfigId") long deviceConfigId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {

        List<ComTaskEnablementInfo> infos = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getComTaskEnablements().stream()
                .map(enablement->comTaskEnablementInfoFactory.from(enablement, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(ComTaskEnablementResource.class)
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    /**
     * Enables the execution of a ComTask against devices of a device configuration and specifies the security
     * requirements for that execution.
     * <br>
     * In addition, specifies preferred scheduling (e.g. every day or every week)
     * and preferred PartialConnectionTask or if the execution of the ComTask
     * should use the default ConnectionTask.
     *
     * @summary Fetch a communication task enablement
     *
     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param comTaskEnablementId Id of the comtask enablement
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     * @return Uniquely identified communication task enablement
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{comTaskEnablementId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public ComTaskEnablementInfo getComTaskEnablement(
            @PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
            @PathParam("comTaskEnablementId") long comTaskEnablementId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {

        ComTaskEnablementInfo info = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(dc -> dc.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getComTaskEnablements().stream()
                .filter(enablement -> enablement.getId() == comTaskEnablementId)
                .findFirst()
                .map(comTaskEnablement -> comTaskEnablementInfoFactory.from(comTaskEnablement, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_COM_TASK_ENABLEMENT));

        return info;
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
        return comTaskEnablementInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
