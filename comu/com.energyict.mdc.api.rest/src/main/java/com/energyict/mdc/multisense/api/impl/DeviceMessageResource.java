package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PROPFIND;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.multisense.api.impl.utils.FieldSelection;
import com.energyict.mdc.multisense.api.impl.utils.MessageSeeds;
import com.energyict.mdc.multisense.api.impl.utils.PagedInfoList;
import com.energyict.mdc.multisense.api.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;

import java.util.List;
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

import static java.util.stream.Collectors.toList;

@Path("/devices/{mrid}/messages")
public class DeviceMessageResource {

    private final DeviceMessageInfoFactory deviceMessageInfoFactory;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageResource(DeviceService deviceService, DeviceMessageInfoFactory deviceMessageInfoFactory, ExceptionFactory exceptionFactory) {
        this.deviceService = deviceService;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Path("/{messageId}")
    @RolesAllowed(Privileges.PUBLIC_REST_API)
    public Response getDeviceMessage(@PathParam("mrid") String mRID, @PathParam("messageId") long id,
                                     @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo) {
        Device device = deviceService.findByUniqueMrid(mRID).orElseThrow(exceptionFactory
                .newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE));
        DeviceMessage<Device> deviceMessage = device.getMessages().stream().filter(msg -> msg.getId() == id).findFirst()
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE));

        return Response.ok(deviceMessageInfoFactory.from(deviceMessage, uriInfo, fieldSelection.getFields())).build();
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.PUBLIC_REST_API)
    public PagedInfoList<DeviceMessageInfo> getDeviceMessages(@PathParam("mrid") String mRID,
                                                              @BeanParam FieldSelection fieldSelection, @Context UriInfo uriInfo,
                                                              @BeanParam JsonQueryParameters queryParameters) {
        List<DeviceMessageInfo> infos = deviceService.findByUniqueMrid(mRID).orElseThrow(exceptionFactory
                .newExceptionSupplier(Response.Status.NOT_FOUND, MessageSeeds.NO_SUCH_DEVICE))
                .getMessages().stream()
                .map(ct -> deviceMessageInfoFactory.from(ct, uriInfo, fieldSelection.getFields()))
                .collect(toList());
        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder()
                .path(DeviceMessageResource.class)
		        .resolveTemplate("mrid", mRID);
        return PagedInfoList.from(infos, queryParameters, uriBuilder, uriInfo);
    }

    @PROPFIND
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @RolesAllowed(Privileges.PUBLIC_REST_API)
    public List<String> getFields() {
        return deviceMessageInfoFactory.getAvailableFields().stream().sorted().collect(toList());
    }

}
