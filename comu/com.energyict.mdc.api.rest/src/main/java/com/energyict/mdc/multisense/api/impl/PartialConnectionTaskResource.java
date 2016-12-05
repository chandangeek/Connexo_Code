package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.PartialConnectionTask;
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
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 7/15/15.
 */
@Path("/devicetypes/{deviceTypeId}/deviceconfigurations/{deviceConfigId}/partialconnectiontasks")
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

    /**
     * Partial version of a ConnectionTask when it comes to
     * the properties required to establish a connection with a device.
     * It also enables the device's capability to use the ConnectionType
     * on the DeviceConfiguration against which it is being created.
     * As an example, a device might have the capability to communicate
     * via TCP/IP, GPRS and infra-red but on the configuration level,
     * the communication expert may decide to only enable TCP/IP and infra-red.
     * This way, it will not be possible to create a ConnectionTask
     * that uses GPRS because that was not enabled.
     *
     * @summary Fetch a partial connection task

     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
     * @param id Id of the partial connection task
     * @param uriInfo uriInfo
     * @param fieldSelection field selection
     *
     * @return Uniquely identified partial connection task
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{id}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PartialConnectionTaskInfo getPartialConnectionTask(@PathParam("deviceTypeId") long deviceTypeId,
                                                              @PathParam("deviceConfigId") long deviceConfigId,
                                                              @PathParam("id") long id,
                                                              @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection) {
        PartialConnectionTask partialConnectionTask = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(config -> config.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getPartialConnectionTasks().stream()
                .filter(task -> task.getId() == id)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_PARTIAL_CONNECTION_TASK));
        return partialConnectionTypeInfoFactory.from(partialConnectionTask, uriInfo, fieldSelection.getFields());
    }

    /**
     * Partial version of a ConnectionTask when it comes to
     * the properties required to establish a connection with a device.
     * It also enables the device's capability to use the ConnectionType
     * on the DeviceConfiguration against which it is being created.
     * As an example, a device might have the capability to communicate
     * via TCP/IP, GPRS and infra-red but on the configuration level,
     * the communication expert may decide to only enable TCP/IP and infra-red.
     * This way, it will not be possible to create a ConnectionTask
     * that uses GPRS because that was not enabled.
     *
     * @summary Fetch a set of partial connection tasks

     * @param deviceTypeId Id of the device type
     * @param deviceConfigId Id of the device configuration
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
    public PagedInfoList<PartialConnectionTaskInfo> getPartialConnectionTasks(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("deviceConfigId") long deviceConfigId,
                                                                              @Context UriInfo uriInfo, @BeanParam FieldSelection fieldSelection, @BeanParam JsonQueryParameters queryParameters) {
        List<PartialConnectionTask> partialConnectionTasks = deviceConfigurationService.
                findDeviceType(deviceTypeId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                .getConfigurations().stream()
                .filter(config -> config.getId() == deviceConfigId)
                .findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG))
                .getPartialConnectionTasks();
        List<PartialConnectionTaskInfo> infos = ListPager.of(partialConnectionTasks, Comparator.comparing(PartialConnectionTask::getName, String.CASE_INSENSITIVE_ORDER))
                .from(queryParameters).stream()
                .map(pct -> partialConnectionTypeInfoFactory.from(pct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(PartialConnectionTaskResource.class)
                .resolveTemplate("deviceTypeId", deviceTypeId)
                .resolveTemplate("deviceConfigId", deviceConfigId);

        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
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
    public List<String> getPartialConnectionTasks() {
        return partialConnectionTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
