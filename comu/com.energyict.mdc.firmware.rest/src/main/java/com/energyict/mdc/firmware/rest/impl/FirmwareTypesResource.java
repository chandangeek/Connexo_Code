package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.security.Privileges;
import com.energyict.mdc.firmware.FirmwareService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Copyrights EnergyICT
 * Date: 07/05/15
 * Time: 13:44
 */
@Path("/devicetypes/{deviceTypeId}/supportedfirmwaretypes")
public class FirmwareTypesResource {

    private final FirmwareService firmwareService;
    private final ResourceHelper resourceHelper;
    private final Thesaurus thesaurus;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public FirmwareTypesResource(FirmwareService firmwareService, ResourceHelper resourceHelper, Thesaurus thesaurus, ExceptionFactory exceptionFactory) {
        this.firmwareService = firmwareService;
        this.resourceHelper = resourceHelper;
        this.thesaurus = thesaurus;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.VIEW_DEVICE_TYPE, Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Response getSupportedFirmwareTypes(@PathParam("deviceTypeId") long deviceTypeId) {
        DeviceType deviceType = resourceHelper.findDeviceTypeOrElseThrowException(deviceTypeId);

        return Response.ok().entity(new FirmwareTypeInfos(deviceType, thesaurus)).build();
    }
}
