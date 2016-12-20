package com.elster.jupiter.autotests.rest.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.util.conditions.Where;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Transactional
    public Response delete(@PathParam("name") String name) throws SQLException {
        Optional<UsagePoint> usagePointOptional = meteringService.findUsagePointByName(name);
        if (usagePointOptional.isPresent()) {
            UsagePoint usagePoint = usagePointOptional.get();

            DataModel dataModel = ormService.getDataModel("MTR").get();
            List<MeterActivation> meterActivations =
                    dataModel.query(MeterActivation.class).select(Where.where("USAGEPOINTID").isEqualTo(usagePoint.getId()));

            try (Connection connection = dataModel.getConnection(true); Statement statement = connection.createStatement()) {
                long usagePointId = usagePoint.getId();
                statement.addBatch(
                        "DELETE FROM MTR_READINGQUALITY WHERE CHANNELID IN (" +
                                "SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN (" +
                                "SELECT CHANNELS_CONTAINER FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " +
                                usagePointId + ")))"
                );
                statement.addBatch(
                        "DELETE FROM VAL_CH_VALIDATION WHERE CHANNELID IN (" +
                                "SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN (" +
                                "SELECT CHANNELS_CONTAINER FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " +
                                usagePointId + ")))"
                );
                statement.addBatch(
                        "DELETE FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN (" +
                                "SELECT CHANNELS_CONTAINER FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " +
                                usagePointId + "))"
                );
                statement.addBatch(
                        "DELETE FROM VAL_MA_VALIDATION WHERE CHANNEL_CONTAINER IN (" +
                                "SELECT CHANNELS_CONTAINER FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " +
                                usagePointId + "))"
                );
                statement.addBatch(
                        "DELETE FROM MTR_EFFECTIVE_CONTRACT WHERE EFFECTIVE_CONF IN (" +
                                "SELECT ID FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " +
                                usagePointId + ")"
                );
                statement.addBatch("DELETE FROM MTR_USAGEPOINTMTRCONFIG WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM ANT_CPS_ANTENNA WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM CON_CPS_USAGEPOINT_CONTRCT WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM GNR_CPS_USAGEPOINT_GNRL WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM TE1_CPS_TECH_EL WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM MIS_CPS_USAGEPOINT_TECH WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM MTC_CPS_USAGEPOINT_GENER WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM COU_CALENDAR_ON_USAGEPOINT WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM COU_CALENDAR_ON_USAGEPOINT WHERE USAGEPOINT = " + usagePointId);
                statement.addBatch("DELETE FROM UPE_REQUEST_FAIL WHERE CHANGE_REQUEST IN (SELECT ID FROM UPE_REQUEST WHERE USAGE_POINT = " + usagePointId + ")");
                statement.addBatch("DELETE FROM UPE_REQUEST_PROPERTY WHERE CHANGE_REQUEST IN (SELECT ID FROM UPE_REQUEST WHERE USAGE_POINT = " + usagePointId + ")");
                statement.addBatch("DELETE FROM UPE_REQUEST WHERE USAGE_POINT = " + usagePointId);

                for (MeterActivation meterActivation : meterActivations) {
                    long meterActivationId = meterActivation.getId();
                    statement.addBatch(
                            "DELETE FROM MTR_READINGQUALITY WHERE CHANNELID IN" +
                                    " ( SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN" +
                                    " ( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                    meterActivationId + "))");
                    statement.addBatch(
                            "DELETE FROM VAL_CH_VALIDATION WHERE CHANNELID IN" +
                                    " ( SELECT ID FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN" +
                                    " ( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                    meterActivationId + "))");
                    statement.addBatch(
                            "DELETE FROM MTR_CHANNEL WHERE CHANNEL_CONTAINER IN " +
                                    "( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                    meterActivationId + ")");
                    statement.addBatch(
                            "DELETE FROM VAL_MA_VALIDATION WHERE CHANNEL_CONTAINER IN " +
                                    "( SELECT ID FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " +
                                    meterActivationId + ")");
                    statement.addBatch("DELETE FROM MTR_CHANNEL_CONTAINER WHERE METER_ACTIVATION = " + meterActivationId);
                    statement.addBatch("DELETE FROM MTR_METERACTIVATION WHERE ID = " + meterActivationId);
                }
                statement.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            usagePoint.delete();
            return Response.status(Response.Status.OK).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}

