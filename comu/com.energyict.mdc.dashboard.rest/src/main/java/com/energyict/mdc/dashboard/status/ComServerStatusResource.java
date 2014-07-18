package com.energyict.mdc.dashboard.status;

import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.engine.status.ComServerStatus;
import com.energyict.mdc.engine.status.StatusService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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

    @Inject
    public ComServerStatusResource(StatusService statusService) {
        super();
        this.statusService = statusService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PagedInfoList getComServerStatus() {
        ComServerStatus status = this.statusService.getStatus();
        return new ComServerStatusInfo(status);
    }

}