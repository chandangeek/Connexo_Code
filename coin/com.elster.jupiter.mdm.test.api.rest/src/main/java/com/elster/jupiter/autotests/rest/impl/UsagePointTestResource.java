package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

@Path("/usagepoints")
public class UsagePointTestResource {

    private final MeteringService meteringService;
    private final OrmService ormService;

    @Inject
    public UsagePointTestResource(MeteringService meteringService, OrmService ormService) {
        this.meteringService = meteringService;
        this.ormService = ormService;
    }

    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response delete(@PathParam("name") String name) throws SQLException {
        Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(name);
        if (usagePoint.isPresent()) {
            usagePoint.get().makeObsolete();
            return Response.status(Response.Status.OK).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/clearhistory/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response clearImportHistory() throws SQLException {
        try (Connection connection = ormService.getDataModel("FIS").get().getConnection(true)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DELETE FROM FIM_FILE_IMPORT_OCCURRENCE");
            }
            return Response.status(Response.Status.OK).build();
        }
    }
}
