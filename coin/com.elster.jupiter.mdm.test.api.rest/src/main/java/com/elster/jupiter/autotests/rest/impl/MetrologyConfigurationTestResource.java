package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.config.MetrologyConfigurationStatus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;

@Path("/metrologyconfigurations")
public class MetrologyConfigurationTestResource {

    private final MetrologyConfigurationService metrologyConfigurationService;
    private final OrmService ormService;

    @Inject
    public MetrologyConfigurationTestResource(MetrologyConfigurationService metrologyConfigurationService, OrmService ormService) {
        this.metrologyConfigurationService = metrologyConfigurationService;
        this.ormService = ormService;
    }

    @PUT
    @Path("/{id}/deactivate")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response deactivate(@PathParam("id") Long id) {
        if (metrologyConfigurationService.findMetrologyConfiguration(id).isPresent()) {
            metrologyConfigurationService.findMetrologyConfiguration(id).get().deactivate();
        }
        return Response.status(Response.Status.OK).build();
    }

    @PUT
    @Path("{id}/inactive")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response setInactive(@PathParam("id") Long id) throws SQLException {
        if (!metrologyConfigurationService.findMetrologyConfiguration(id).isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        MetrologyConfiguration metrologyConfiguration = metrologyConfigurationService.findMetrologyConfiguration(id)
                .get();
        if (metrologyConfiguration.getStatus() == MetrologyConfigurationStatus.DEPRECATED) {
            try (Connection connection = ormService.getDataModel("MTR").get().getConnection(true)) {
                connection.createStatement().execute("UPDATE MTR_METROLOGYCONFIG SET STATUS = 0 WHERE ID = " + id);
            }
            return Response.status(Response.Status.OK).build();
        }

        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
