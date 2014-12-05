package com.elster.jupiter.yellowfin.rest.impl;


import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinReportInfo;
import com.elster.jupiter.yellowfin.YellowfinService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/report")
public class YellowfinReportInfoResource {

    private YellowfinService yellowfinService;



    @Inject
    private YellowfinReportInfoResource(YellowfinService yellowfinService){
        this.yellowfinService = yellowfinService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/info")
    public ReportInfos getReportsInfo(@QueryParam("category") String category,
                                      @QueryParam("subCategory") String subCategory,
                                      @Context SecurityContext securityContext){

        User user = (User) securityContext.getUserPrincipal();
        ReportInfos reportInfos = new ReportInfos();
        reportInfos.addAll(yellowfinService.getUserReports(user.getName(), category, subCategory,null));
        return reportInfos;

    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter")
    public FilterInfos getFiltersInfo(@QueryParam("id") int id,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @Context SecurityContext securityContext){
        FilterInfos filterInfos = new FilterInfos();
        User user = (User) securityContext.getUserPrincipal();

        if (id!=0) {
            filterInfos.addAll(yellowfinService.getReportFilters(id));
        }else{
            if (reportUUID !=null) {
                ReportInfos reportInfos = new ReportInfos();
                reportInfos.addAll(yellowfinService.getUserReports(user.getName(), null, null, reportUUID));
                if (reportInfos.total>0) {
                    List<ReportInfo> reportInfo = new ArrayList<>();
                    reportInfo = reportInfos.reports;
                    filterInfos.addAll(yellowfinService.getReportFilters(reportInfo.get(0).getReportId()));

                }

            }
        }
        return filterInfos;


    }


}
