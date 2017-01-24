package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceMessageFile;
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
 * RESTfull resource for {@link DeviceMessageFile}s.
 */
@Path("/devicetypes/{deviceTypeId}/devicemessagefiles")
public class DeviceMessageFileResource {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceMessageFileInfoFactory deviceMessageFileInfoFactory;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageFileResource(DeviceConfigurationService deviceConfigurationService, DeviceMessageFileInfoFactory deviceMessageFileInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceConfigurationService = deviceConfigurationService;
        this.deviceMessageFileInfoFactory = deviceMessageFileInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    /**
     * Device message files are sent to physical devices.
     * The list of available files are limited to avoid that the
     * device operator can send any file to the device that may
     * harm the device when received.
     *
     * @summary Fetch a device message file
     *
     * @param deviceTypeId Id of the device type
     * @param id Id of the device message file
     * @param uriInfo uriInfo
     * @param fields fields
     * @return Uniquely identified device message file
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{deviceMessageFileId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceMessageFileInfo getDeviceMessageFile(
            @PathParam("deviceTypeId") long deviceTypeId,
            @PathParam("deviceMessageFileId") long id,
            @BeanParam FieldSelection fields, @Context UriInfo uriInfo) {
        return deviceConfigurationService
                    .findDeviceType(deviceTypeId)
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                    .getDeviceMessageFiles()
                    .stream()
                    .filter(each -> each.getId() == id)
                    .map(each -> deviceMessageFileInfoFactory.from(each, uriInfo, fields.getFields()))
                    .findFirst()
                    .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_CONFIG));
    }

    /**
     * Device message files are sent to physical devices.
     * The list of available files are limited to avoid that the
     * device operator can send any file to the device that may
     * harm the device when received.
     *
     * @summary Fetch a set of device message files
     *
     * @param deviceTypeId Id of the device type
     * @param uriInfo uriInfo
     * @param queryParameters queryParameters
     * @param fields fields
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     */
    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceMessageFileInfo> getHypermediaDeviceMessageFiles(
            @PathParam("deviceTypeId") long deviceTypeId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fields,@Context UriInfo uriInfo) {
        List<DeviceMessageFile> allFiles =
                deviceConfigurationService
                        .findDeviceType(deviceTypeId)
                        .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_TYPE))
                        .getDeviceMessageFiles();
        List<DeviceMessageFileInfo> infos = ListPager.of(allFiles).from(queryParameters).find().stream().map(each -> deviceMessageFileInfoFactory.from(each, uriInfo, fields.getFields())).collect(toList());
        UriBuilder uri = uriInfo.getBaseUriBuilder().path(DeviceMessageFileResource.class).resolveTemplate("deviceMessageFileId", deviceTypeId);
        return PagedInfoList.from(infos, queryParameters, uri, uriInfo);
    }

    /**
     * List the fields available on this type of entity.
     * <br>E.g.
     * <br>[
     * <br> "id",
     * <br> "name",
     * <br>]
     * <br>Fields in the list can be used as parameter on a GET request to the same resource, e.g.
     * <br> <i></i>GET ..../resource?fields=id,name</i>
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
        return deviceMessageFileInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}