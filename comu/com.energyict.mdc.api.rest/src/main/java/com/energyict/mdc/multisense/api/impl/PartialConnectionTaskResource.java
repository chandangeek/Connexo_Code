package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import java.util.List;
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

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/15/15.
 */
@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/connectionmethods")
public class PartialConnectionTaskResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final PartialConnectionTaskInfoFactory partialConnectionTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public PartialConnectionTaskResource(DeviceConfigurationService deviceConfigurationService, PartialConnectionTaskInfoFactory partialConnectionTypeInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.partialConnectionTypeInfoFactory = partialConnectionTypeInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{id}")
    public Response getPartialConnectionTask(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId, @PathParam("id") long id,
                                             @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        PartialConnectionTask partialConnectionTask = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(config -> config.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getPartialConnectionTasks().stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK));
        PartialConnectionTaskInfo info = partialConnectionTypeInfoFactory.from(partialConnectionTask, uriInfo, fieldSelection.getFields());
        return Response.ok(info).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response getPartialConnectionTasks(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
                                             @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection, @BeanParam JsonQueryParameters queryParameters) {
        List<PartialConnectionTask> partialConnectionTasks = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(config -> config.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getPartialConnectionTasks();
        List<PartialConnectionTaskInfo> infos = ListPager.of(partialConnectionTasks, (pct1, pct2) -> (pct1.getName().compareToIgnoreCase(pct2.getName())))
                .from(queryParameters).stream()
                .map(pct -> partialConnectionTypeInfoFactory.from(pct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(PartialConnectionTaskResource.class)
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId);

        return Response.ok(PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo)).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/fields")
    public Response getPartialConnectionTasks() {
        return Response.ok(partialConnectionTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList())).build();
    }


}
