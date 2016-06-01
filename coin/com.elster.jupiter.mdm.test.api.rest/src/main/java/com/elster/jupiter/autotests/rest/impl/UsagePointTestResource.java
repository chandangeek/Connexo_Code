package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.Transactional;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;

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
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response delete(@PathParam("mRID") String mRID, UsagePointInfo usagePointInfo) throws SQLException {
        if (this.meteringService.findUsagePoint(mRID).isPresent()) {
            UsagePoint usagePoint = this.meteringService.findUsagePoint(mRID).get();
            if (usagePoint.getCurrentMeterActivations().size() != 0) {
                DataModel dataModel = ormService.getDataModel("MTR").get();
                Connection connection = dataModel.getConnection(true);
                usagePoint.getCurrentMeterActivations().stream().forEach(meterActivation -> {
                    try {
                        connection.createStatement()
                                .execute("UPDATE MTR_METERACTIVATION SET USAGEPOINTID =  NULL WHERE USAGEPOINTID = " + usagePoint
                                        .getId());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
            usagePoint.delete();
            return Response.status(Response.Status.OK).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
