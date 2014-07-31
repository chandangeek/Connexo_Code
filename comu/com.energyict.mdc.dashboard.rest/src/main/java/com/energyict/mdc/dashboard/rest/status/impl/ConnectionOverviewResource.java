package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.DashboardService;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * Created by bvn on 7/29/14.
 */
@Path("/connectionoverview")
public class ConnectionOverviewResource {

    private final Thesaurus thesaurus;
    private final DashboardService dashboardService;

    @Inject
    public ConnectionOverviewResource(Thesaurus thesaurus, DashboardService dashboardService) {
        this.thesaurus = thesaurus;
        this.dashboardService = dashboardService;
    }

    @GET
    @Consumes("application/json")
    public ConnectionOverviewInfo getConnectionOverview() {
        ConnectionOverviewInfo info = null;
        try {
            info = new ConnectionOverviewInfo(dashboardService,thesaurus);
        } catch (Exception e) {
            e.printStackTrace();
        }

//
//        info.breakdownPerCommunicationPortPool=new HashMap<>();
//        info.breakdownPerCommunicationPortPool.put("GPRS", createBreakdown(21, 15, 6));
//        info.breakdownPerCommunicationPortPool.put("EIWeb", createBreakdown(15, 5, 15));
//        info.breakdownPerCommunicationPortPool.put("PSTN", createBreakdown(11, 4, 40));
//        info.breakdownPerCommunicationPortPool.put("TCP Inbound", createBreakdown(5, 7, 10));
//        info.breakdownPerCommunicationPortPool.put("PLC", createBreakdown(3, 1, 11));
//        info.breakdownPerCommunicationPortPool.put("TCP Inbound 2", createBreakdown(2, 99, 11));
//
//        info.breakdownPerConnectionType=new HashMap<>();
//        info.breakdownPerConnectionType.put("Outbound IP", createBreakdown(25,11,11));
//        info.breakdownPerConnectionType.put("Outbound IP post dial", createBreakdown(10,5,30));
//        info.breakdownPerConnectionType.put("Outbound UDP", createBreakdown(2,3,27));
//        info.breakdownPerConnectionType.put("Serial", createBreakdown(2,3,42));
//        info.breakdownPerConnectionType.put("Serial PEMP", createBreakdown(2,3,52));
//        info.breakdownPerConnectionType.put("Serial PTPP", createBreakdown(1,99,5));
//        info.breakdownPerConnectionType.put("GPRS", createBreakdown(0,200,0));
//
//        info.breakdownPerDeviceType=new HashMap<>();
//        info.breakdownPerDeviceType.put("Actaris SL2000", createBreakdown(13, 10, 10));
//        info.breakdownPerDeviceType.put("XMR 5900", createBreakdown(12, 6, 10));
//        info.breakdownPerDeviceType.put("Elster 1200", createBreakdown(9, 5, 23));
//        info.breakdownPerDeviceType.put("Elster 3200", createBreakdown(3, 6, 9));
//        info.breakdownPerDeviceType.put("Elster 100", createBreakdown(1, 9, 1));
//        info.breakdownPerDeviceType.put("Elster 1", createBreakdown(0, 0, 99));

        return info;
    }
}
