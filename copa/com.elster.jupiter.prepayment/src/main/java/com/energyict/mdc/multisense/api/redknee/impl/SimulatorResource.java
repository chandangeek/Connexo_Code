package com.energyict.mdc.multisense.api.redknee.impl;

import com.elster.jupiter.metering.UsagePoint;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 9/16/15.
 */
@Path("/simulator")
public class SimulatorResource {
    private final ConsumptionExportGenerator consumptionExportGenerator;

    @Inject
    public SimulatorResource(ConsumptionExportGenerator consumptionExportGenerator) {
        this.consumptionExportGenerator = consumptionExportGenerator;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response getCurrentConfig() {
        return Response.ok().entity(getSimulatorInfo()).build();
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON+";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON+";charset=UTF-8")
    public Response setConfig(SimulatorInfo simulatorInfo) {
        consumptionExportGenerator.setPath(simulatorInfo.path);
        consumptionExportGenerator.setConsumption(simulatorInfo.consumption);
        consumptionExportGenerator.setUsagePoints(Collections.emptyList());
        consumptionExportGenerator.setIntervalInSeconds(simulatorInfo.frequency);

        return Response.ok().entity(getSimulatorInfo()).build();
    }

    private SimulatorInfo getSimulatorInfo() {
        SimulatorInfo simulatorInfo = new SimulatorInfo();
        simulatorInfo.consumption = consumptionExportGenerator.getConsumption();
        simulatorInfo.frequency = consumptionExportGenerator.getIntervalInSeconds();
        simulatorInfo.path = consumptionExportGenerator.getPath();
        simulatorInfo.usagePoints = consumptionExportGenerator.getUsagePoints().stream().map(UsagePoint::getMRID).collect(toList());
        return simulatorInfo;
    }


    static class SimulatorInfo {
        public long consumption;
        public long frequency;
        public List<String> usagePoints;
        public String path;
    }
}
