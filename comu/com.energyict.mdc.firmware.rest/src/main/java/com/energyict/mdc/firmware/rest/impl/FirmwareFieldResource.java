package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.FieldResource;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/field")
public class FirmwareFieldResource extends FieldResource {

    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final FirmwareMessageInfoFactory firmwareMessageInfoFactory;

    @Inject
    public FirmwareFieldResource(Thesaurus thesaurus, FirmwareService firmwareService, ResourceHelper resourceHelper, FirmwareMessageInfoFactory firmwareMessageInfoFactory) {
        super(thesaurus);
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.firmwareMessageInfoFactory = firmwareMessageInfoFactory;
    }

    @GET
    @Path("/firmwareStatuses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareStatuses() {
        return asJsonArrayObjectWithTranslation("firmwareStatuses", "id", new FirmwareStatusFieldAdapter().getClientSideValues());
    }

    @GET
    @Path("/firmwareTypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.Constants.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareTypes(@QueryParam("deviceType") Long deviceTypeId) {
        List<String> firmwareTypes = new FirmwareTypeFieldAdapter().getClientSideValues();
        if (deviceTypeId != null){
            DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
            if (!deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().supportsCommunicationFirmwareVersion()){
                firmwareTypes.remove(MessageSeeds.Keys.TYPE_COMMUNICATION);
            }
        }
        return asJsonArrayObjectWithTranslation("firmwareTypes", "id", firmwareTypes);
    }

    @GET
    @Path("/devicetypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getDeviceTypesWhichSupportFirmwareUpgrade() {
        List<IdWithLocalizedValue> deviceTypes = firmwareService.getDeviceTypesWhichSupportFirmwareManagement()
                .stream()
                .map(IdWithLocalizedValue::from)
                .collect(Collectors.toList());
        return Response.ok(deviceTypes).build();
    }

    @GET
    @Path("/devicetypes/{deviceTypeId}/{firmwareOption}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.Constants.VIEW_DEVICE_TYPE})
    public Response getUploadOptionSpecForDeviceType(@PathParam("deviceTypeId") long deviceTypeId, @PathParam("firmwareOption") String firmwareOption, @QueryParam("firmwareType") String firmwareType) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);
        DeviceMessageSpec firmwareMessageSpec = resourceHelper.findFirmwareMessageSpecOrThrowException(deviceType, firmwareOption);
        return Response.ok(firmwareMessageInfoFactory.from(firmwareMessageSpec, deviceType, firmwareOption, firmwareType)).build();
    }
}
