package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.rest.FieldResource;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/field")
public class FirmwareFieldResource extends FieldResource {
    
    @Inject
    public FirmwareFieldResource(Thesaurus thesaurus) {
        super(thesaurus);
    }

    @GET
    @Path("/firmwareStatuses")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareStatuses() {
        return asJsonArrayObjectWithTranslation("firmwareStatuses", "id", new FirmwareStatusFieldAdapter().getClientSideValues());
    }

    @GET
    @Path("/firmwareTypes")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({com.energyict.mdc.device.config.security.Privileges.VIEW_DEVICE_TYPE, com.energyict.mdc.device.config.security.Privileges.ADMINISTRATE_DEVICE_TYPE})
    public Object getFirmwareTypes() {
        return asJsonArrayObjectWithTranslation("firmwareTypes", "id", new FirmwareTypeFieldAdapter().getClientSideValues());
    }
}
