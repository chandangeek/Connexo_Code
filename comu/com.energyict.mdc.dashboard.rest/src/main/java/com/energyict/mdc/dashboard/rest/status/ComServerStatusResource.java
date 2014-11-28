package com.energyict.mdc.dashboard.rest.status;

import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;
import com.energyict.mdc.engine.model.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Models the REST resource that gets the status of the {@link com.energyict.mdc.engine.model.ComServer}
 * that is configured to run in this instance of the MDC application.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (17:27)
 */
@Path("/comserverstatus")
public class ComServerStatusResource {

    private final StatusService statusService;
    private final ComServerStatusInfoFactory comServerStatusInfoFactory;

    @Inject
    public ComServerStatusResource(StatusService statusService, ComServerStatusInfoFactory comServerStatusInfoFactory) {
        super();
        this.statusService = statusService;
        this.comServerStatusInfoFactory = comServerStatusInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_COMMUNICATION_INFRASTRUCTURE,Privileges.VIEW_COMMUNICATION_INFRASTRUCTURE})
    public ComServerStatusInfo getComServerStatus(@Context UriInfo uriInfo) {
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getBaseUri()).path(ComServerStatusResource.class).host("{host}");
        ComServerStatus status = this.statusService.getStatus();
        String defaultUri = uriBuilder.build(status.getComServerName()).toString();
        return comServerStatusInfoFactory.from(status, defaultUri);
    }

}