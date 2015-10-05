package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.services.ListPager;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{encryptionDeviceAccessLevelId}")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
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
                .map(lvl -> encryptionDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_ENC_DEVICE_ACCESS_LEVEL));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public PagedInfoList<DeviceAccessLevelInfo> getEncryptionDeviceAccessLevels(
            @PathParam("deviceProtocolPluggableClassId") long deviceProtocolPluggableClassId,
            @PathParam("encryptionDeviceAccessLevelId") long encryptionDeviceAccessLevelId,
            @BeanParam JsonQueryParameters queryParameters,
            @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        DeviceProtocolPluggableClass pluggableClass = protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId)
                .orElseThrow(exceptionFactory.newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE_PROTOCOL));
        List<DeviceAccessLevelInfo> infos = ListPager.of(pluggableClass.getDeviceProtocol().getEncryptionAccessLevels()).from(queryParameters)
                .stream()
                .map(lvl -> encryptionDeviceAccessLevelInfoFactory.from(pluggableClass, lvl, uriInfo, fieldSelection.getFields()))
                .collect(toList());

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(EncryptionDeviceAccessLevelResource.class)
                .resolveTemplate("deviceProtocolPluggableClassId", deviceProtocolPluggableClassId);
        return PagedInfoList.from(infos,queryParameters,uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed({Privileges.PUBLIC_REST_API})
    public List<String> getFields() {
        return encryptionDeviceAccessLevelInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }



}
