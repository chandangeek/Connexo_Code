package com.energyict.mdc.multisense.api.impl;


import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrId}/keyAccessorTypes")
public class KeyAccessorTypeResource {

    private final KeyAccessorTypeInfoFactory keyAccessorTypeInfoFactory;
    private final ExceptionFactory exceptionFactory;
    private final DeviceService deviceService;

    @Inject
    public KeyAccessorTypeResource(DeviceService deviceService, ExceptionFactory exceptionFactory, KeyAccessorTypeInfoFactory keyAccessorTypeInfoFactory) {
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.keyAccessorTypeInfoFactory = keyAccessorTypeInfoFactory;
    }

    /**
     * Renews the keys for the devices for the given key accessor type.
     *
     * @param mrId              mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the key accessor type
     * @param uriInfo           uriInfo
     * @summary renews the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/renew")
    public Response renewKey(@PathParam("mrId") String mrId, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                             @Context UriInfo uriInfo) {

        Device device = deviceService.findDeviceByMrid(mrId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        KeyAccessorType keyAccessorType = getKeyAccessorType(keyAccessorTypeId, device.getDeviceType());
        device.getKeyAccessor(keyAccessorType).get().renew();
        return Response.ok().build();
    }

    /**
     * Switch the keys for the devices for the given key accessor type.
     *
     * @param mrId              mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the key accessor type
     * @param uriInfo           uriInfo
     * @summary switches the keys for the device / keyAccessorType (temp - actual)
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/switch")
    public Response switchKey(@PathParam("mrId") String mrId, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                             @Context UriInfo uriInfo) {

        Device device = deviceService.findDeviceByMrid(mrId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        KeyAccessorType keyAccessorType = getKeyAccessorType(keyAccessorTypeId, device.getDeviceType());
        device.getKeyAccessor(keyAccessorType).get().swapValues();

        return Response.ok().build();
    }

    /**
     * Clears the temp value of the keys for the devices for the given key accessor type.
     *
     * @param mrId              mRID of device for which the key will be updated
     * @param keyAccessorTypeId Identifier of the key accessor type
     * @param uriInfo           uriInfo
     * @summary clears the temp value of the key for the device / keyAccessorType
     */
    @PUT
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    @Path("/{keyAccessorTypeId}/clear")
    public Response clearTempValue(@PathParam("mrId") String mrId, @PathParam("keyAccessorTypeId") long keyAccessorTypeId,
                              @Context UriInfo uriInfo) {

        Device device = deviceService.findDeviceByMrid(mrId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE));
        KeyAccessorType keyAccessorType = getKeyAccessorType(keyAccessorTypeId, device.getDeviceType());
        device.getKeyAccessor(keyAccessorType).get().clearTempValue();

        return Response.ok().build();
    }

    /**
     * View the contents of a keyAccessorType for a device.
     *
     * @param name           The name of the keyAccessorType
     * @param uriInfo        uriInfo
     * @param fieldSelection field selection
     * @return Device information and links to related resources
     * @summary View keyAccessortype identified by name for a device
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{keyAccessorTypeName}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public KeyAccessorTypeInfo getKeyAccessorType(@PathParam("mrId") String mrId, @PathParam("keyAccessorTypeName") String name, @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceType devicetype = deviceService.findDeviceByMrid(mrId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.CONFLICT, MessageSeeds.NO_SUCH_DEVICE))
                .getDeviceType();
        KeyAccessorType keyAccessorType = getKeyAccessorType(name, devicetype);
        return keyAccessorTypeInfoFactory.from(keyAccessorType, uriInfo, fieldSelection.getFields());
    }

    private KeyAccessorType getKeyAccessorType(String name, DeviceType devicetype) {
        return devicetype.getKeyAccessorTypes().stream().filter(keyAccessorType1 -> keyAccessorType1.getName().equals(name)).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
    }

    private KeyAccessorType getKeyAccessorType(long id, DeviceType devicetype) {
        return devicetype.getKeyAccessorTypes().stream().filter(keyAccessorType1 -> keyAccessorType1.getId() == id).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_KEYACCESSORTYPE_FOR_DEVICE));
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
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return keyAccessorTypeInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}

