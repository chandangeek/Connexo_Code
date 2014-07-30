package com.energyict.mdc.dashboard.rest.status;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/connectionoverview")
public class ConnectionOverviewResource {

    @GET
    @Consumes("application/json")
    public ConnectionOverviewInfo getConnectionOverview() {
        ConnectionOverviewInfo info = new ConnectionOverviewInfo();

        info.connectionSummary = new ConnectionSummaryInfo();
        info.connectionSummary.activeConnections=594;
        info.connectionSummary.successfulConnections=245;
        info.connectionSummary.connectionsWithAllTasksSuccessful=83;
        info.connectionSummary.connectionsWithFailingTasks=162;
        info.connectionSummary.pendingConnections=62;
        info.connectionSummary.failedConnections=42;

        info.connectionsOverviewPerCurrentState=new HashMap<>();
        info.connectionsOverviewPerCurrentState.put("Pending", 42);
        info.connectionsOverviewPerCurrentState.put("Busy", 41);
        info.connectionsOverviewPerCurrentState.put("Failed", 41);
        info.connectionsOverviewPerCurrentState.put("On hold", 27);
        info.connectionsOverviewPerCurrentState.put("Retrying", 15);
        info.connectionsOverviewPerCurrentState.put("Never completed", 15);
        info.connectionsOverviewPerCurrentState.put("Waiting", 15);

        info.connectionsOverviewPerLastResult=new HashMap<>();
        info.connectionsOverviewPerLastResult.put("Success", 101);
        info.connectionsOverviewPerLastResult.put("Broken", 51);
        info.connectionsOverviewPerLastResult.put("Setup error", 48);
        info.connectionsOverviewPerLastResult.put("At least one task failed", 45);

        info.breakdownPerCommunicationPortPool=new HashMap<>();
        info.breakdownPerCommunicationPortPool.put("GPRS", createBreakdown(21, 15, 6));
        info.breakdownPerCommunicationPortPool.put("EIWeb", createBreakdown(15, 5, 15));
        info.breakdownPerCommunicationPortPool.put("PSTN", createBreakdown(11, 4, 40));
        info.breakdownPerCommunicationPortPool.put("TCP Inbound", createBreakdown(5, 7, 10));
        info.breakdownPerCommunicationPortPool.put("PLC", createBreakdown(3, 1, 11));
        info.breakdownPerCommunicationPortPool.put("TCP Inbound 2", createBreakdown(2, 99, 11));

        info.breakdownPerConnectionType=new HashMap<>();
        info.breakdownPerConnectionType.put("Outbound IP", createBreakdown(25,11,11));
        info.breakdownPerConnectionType.put("Outbound IP post dial", createBreakdown(10,5,30));
        info.breakdownPerConnectionType.put("Outbound UDP", createBreakdown(2,3,27));
        info.breakdownPerConnectionType.put("Serial", createBreakdown(2,3,42));
        info.breakdownPerConnectionType.put("Serial PEMP", createBreakdown(2,3,52));
        info.breakdownPerConnectionType.put("Serial PTPP", createBreakdown(1,99,5));
        info.breakdownPerConnectionType.put("GPRS", createBreakdown(0,200,0));

        info.breakdownPerDeviceType=new HashMap<>();
        info.breakdownPerDeviceType.put("Actaris SL2000", createBreakdown(13, 10, 10));
        info.breakdownPerDeviceType.put("XMR 5900", createBreakdown(12, 6, 10));
        info.breakdownPerDeviceType.put("Elster 1200", createBreakdown(9, 5, 23));
        info.breakdownPerDeviceType.put("Elster 3200", createBreakdown(3, 6, 9));
        info.breakdownPerDeviceType.put("Elster 100", createBreakdown(1, 9, 1));
        info.breakdownPerDeviceType.put("Elster 1", createBreakdown(0, 0, 99));

        return info;
    }

    private Map<String, Integer> createBreakdown(int failed, int success, int pending) {
        HashMap<String, Integer> value = new HashMap<>();
        value.put("Failed", failed);
        value.put("Success", success);
        value.put("Pending", pending);
        return value;
    }
}
