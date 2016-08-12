package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
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
import java.util.List;
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
    @Path("/{mRID}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response delete(@PathParam("mRID") String mRID, UsagePointInfo usagePointInfo) throws SQLException {
        Optional<UsagePoint> usagePointOptional = meteringService.findUsagePoint(mRID);
        if (usagePointOptional.isPresent()) {
            UsagePoint usagePoint = usagePointOptional.get();
            List<MeterActivation> meterActivations = usagePoint.getMeterActivations();

            Connection connection = ormService.getDataModel("MTR").get().getConnection(true);

            long usagePointId = usagePoint.getId();
            try {
                connection.createStatement()
                        .execute(
                                "DELETE FROM MTR_READINGQUALITY WHERE CHANNELID IN (" +
                                        "SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN (" +
                                        "SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE EFFECTIVE_CONTRACT IN (" +
                                        "SELECT ID FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                        "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT =" +
                                        usagePointId + "))))"
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM VAL_CH_VALIDATION WHERE CHANNELID IN (" +
                                        "SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN (" +
                                        "SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE EFFECTIVE_CONTRACT IN (" +
                                        "SELECT ID FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                        "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT =" +
                                        usagePointId + "))))"
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN (" +
                                        "SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE EFFECTIVE_CONTRACT IN (" +
                                        "SELECT ID FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                        "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT =" +
                                        usagePointId + ")))"
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM VAL_MA_VALIDATION WHERE CHANNEL_CONTAINER IN (" +
                                        "SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE EFFECTIVE_CONTRACT IN (" +
                                        "SELECT ID FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                        "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT =" +
                                        usagePointId + ")))"
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM MTR_CHANNEL_CONTAINER WHERE EFFECTIVE_CONTRACT IN (" +
                                        "SELECT ID FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                        "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT =" +
                                        usagePointId + "))"
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                        "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT =" +
                                        usagePointId + ")"
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " + usagePointId
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM ANT_CPS_ANTENNA WHERE USAGEPOINT = " + usagePointId
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM CON_CPS_USAGEPOINT_CONTRCT WHERE USAGEPOINT = " + usagePointId
                        );
                connection.createStatement()
                        .execute(
                                "DELETE FROM GNR_CPS_USAGEPOINT_GNRL WHERE USAGEPOINT  = " + usagePointId
                        );
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (!meterActivations.isEmpty()) {
                meterActivations.forEach(meterActivation -> {
                    try {
                        long meterActivationId = meterActivation.getId();
                        connection.createStatement()
                                .execute("DELETE FROM MTR_READINGQUALITY WHERE CHANNELID IN" +
                                        " ( SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN" +
                                        " ( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                        meterActivationId + "))");
                        connection.createStatement()
                                .execute("DELETE FROM VAL_CH_VALIDATION WHERE CHANNELID IN" +
                                        " ( SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN" +
                                        " ( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                        meterActivationId + "))");
                        connection.createStatement()
                                .execute("DELETE FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN " +
                                        "( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                        meterActivationId + ")");
                        connection.createStatement()
                                .execute("DELETE FROM VAL_MA_VALIDATION WHERE CHANNEL_CONTAINER IN " +
                                        "( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                        meterActivationId + ")");
                        connection.createStatement()
                                .execute("DELETE FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " + meterActivationId);
                        connection.createStatement()
                                .execute("DELETE FROM MTR_METERACTIVATION WHERE ID = " + meterActivationId);
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

