package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<ComTaskEnablementInfo> getComTaskEnablements(
            @PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
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

    @GET
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

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return comTaskEnablementInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
