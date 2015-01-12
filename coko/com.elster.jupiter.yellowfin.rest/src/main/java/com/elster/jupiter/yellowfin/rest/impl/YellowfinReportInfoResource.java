package com.elster.jupiter.yellowfin.rest.impl;


import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;
import com.hof.mi.web.service.ReportRow;

import javax.annotation.security.RolesAllowed;
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
    @RolesAllowed(Privileges.VIEW_REPORTS)
    public ReportInfos getReportsInfo(@QueryParam("category") String category,
                                      @QueryParam("subCategory") String subCategory,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @Context SecurityContext securityContext){

        User user = (User) securityContext.getUserPrincipal();
        ReportInfos reportInfos = new ReportInfos();
        reportInfos.addAll(yellowfinService.getUserReports(user.getName(), category, subCategory,reportUUID));
        return reportInfos;

    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filter")
    @RolesAllowed(Privileges.VIEW_REPORTS)
    public FilterInfos getFiltersInfo(@QueryParam("reportId") int reportId,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @QueryParam("listAll") boolean listAll,
                                      @Context SecurityContext securityContext){
        FilterInfos filterInfos = new FilterInfos();
        User user = (User) securityContext.getUserPrincipal();

        if (reportId!=0) {
            filterInfos.addAll(yellowfinService.getReportFilters(reportId));
        }else {
            if (reportUUID != null) {
                ReportInfos reportInfos = new ReportInfos();
                reportInfos.addAll(yellowfinService.getUserReports(user.getName(), null, null, reportUUID));
                if (reportInfos.total > 0) {
                    List<ReportInfo> reportInfo = reportInfos.reports;
                    filterInfos.addAll(yellowfinService.getReportFilters(reportInfo.get(0).getReportId()));
                }

            }
        }
        return filterInfos;


    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/filterlistitems")
    @RolesAllowed(Privileges.VIEW_REPORTS)
    public FilterListItemInfos getFiltersInfo(@QueryParam("reportId") int reportId,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @QueryParam("filterId") String filterId,
                                      @Context SecurityContext securityContext){
        FilterListItemInfos filterInfos = new FilterListItemInfos();
        User user = (User) securityContext.getUserPrincipal();

        if (reportId!=0) {
            filterInfos.addAll(yellowfinService.getFilterListItems(filterId, reportId));
        }else {
            if (reportUUID != null) {
                ReportInfos reportInfos = new ReportInfos();
                reportInfos.addAll(yellowfinService.getUserReports(user.getName(), null, null, reportUUID));
                if (reportInfos.total > 0) {
                    List<ReportInfo> reportInfo = new ArrayList<>();
                    reportInfo = reportInfos.reports;
                    filterInfos.addAll(yellowfinService.getFilterListItems(filterId, reportInfo.get(0).getReportId()));
                }

            }
        }
        return filterInfos;


    }

//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    @Path("/a")
//    public String getAllFiltersList(@QueryParam("id") int id,
//                                      @QueryParam("filterID") String filterID){
//
////        ReportRow[] reportRow = null;
////        System.out.println();
////        reportRow = yellowfinService.getFilterList("54856",54855);
//
//
//
//
//        return "jasjda";
//
//    }





}
