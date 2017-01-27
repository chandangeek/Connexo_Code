package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.FieldSelection;
import com.elster.jupiter.rest.api.util.v1.hypermedia.PagedInfoList;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

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

@Path("pluggableclasses/{deviceProtocolPluggableClassId}/encryptionaccesslevels")
public class EncryptionDeviceAccessLevelResource {

    private final EncryptionDeviceAccessLevelInfoFactory encryptionDeviceAccessLevelInfoFactory;
    private final ProtocolPluggableService protocolPluggableService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public EncryptionDeviceAccessLevelResource(EncryptionDeviceAccessLevelInfoFactory encryptionDeviceAccessLevelInfoFactory, ProtocolPluggableService protocolPluggableService, ExceptionFactory exceptionFactory) {
        this.encryptionDeviceAccessLevelInfoFactory = encryptionDeviceAccessLevelInfoFactory;
        this.protocolPluggableService = protocolPluggableService;
        this.exceptionFactory = exceptionFactory;
    }


    /**
     * An encryption device access level models a level of security for a physical device
     * and the PropertySpecs that the device will require to be specified before accessing the data that is
     * secured by this level.
     *
     * @param deviceProtocolPluggableClassId Id of the device protocol pluggable class
     * @param encryptionDeviceAccessLevelId  Id of the encryption device access level
     * @param uriInfo                        uriInfo
     * @param fieldSelection                 field selection
     * @return Uniquely identified encryption device access level
     * @summary Fetch an encryption device access level
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Path("/{encryptionDeviceAccessLevelId}")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public DeviceAccessLevelInfo getEncryptionDeviceAccessLevel(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("encryptionDeviceAccessLevelId") long encryptionDeviceAccessLevelId,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        return pluggableClass.getDeviceProtocol()
                .getEncryptionAccessLevels()
                .stream()
                .filter(lvl -> lvl.getId() == encryptionDeviceAccessLevelId)
                .findFirst()
                .map(this.protocolPluggableService::adapt)
                .map(lvl -> encryptionDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ENC_DEVICE_ACCESS_LEVEL));
    }

    /**
     * An encryption device access level models a level of security for a physical device
     * and the PropertySpecs that the device will require to be specified before accessing the data that is
     * secured by this level.
     *
     * @param deviceProtocolPluggableClassId Id of the device protocol pluggable class
     * @param uriInfo                        uriInfo
     * @param fieldSelection                 field selection
     * @param queryParameters                queryParameters
     * @return a sorted, pageable list of elements. Only fields mentioned in field-param will be provided, or all fields if no
     * field-param was provided. The list will be sorted according to db order.
     * @summary Fetch a set of encryption device access levels
     */
    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public PagedInfoList<DeviceAccessLevelInfo> getEncryptionDeviceAccessLevels(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        List<DeviceAccessLevelInfo> infos = ListPager.of(pluggableClass.getDeviceProtocol().getEncryptionAccessLevels()).from(queryParameters)
                .stream()
                .map(this.protocolPluggableService::adapt)
                .map(lvl -> encryptionDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(EncryptionDeviceAccessLevelResource.class)
                .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClassId);
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
     * @return A list of field names that can be requested as parameter in the GET method on this entity type
     * @summary List the fields available on this type of entity
     */
    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @RolesAllowed({Privileges.Constants.PUBLIC_REST_API})
    public List<String> getFields() {
        return encryptionDeviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }


}
