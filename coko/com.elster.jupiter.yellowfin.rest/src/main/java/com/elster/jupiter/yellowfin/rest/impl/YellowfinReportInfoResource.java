package com.elster.jupiter.yellowfin.rest.impl;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinReportInfo;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

@Path("/report")
public class YellowfinReportInfoResource {

    private YellowfinService yellowfinService;
    private final String errorMessage;


    @Inject
    private YellowfinReportInfoResource(YellowfinService yellowfinService, Thesaurus thesaurus){
        this.yellowfinService = yellowfinService;
        this.errorMessage = thesaurus.getString("error.facts.unavailable", "Connexo Facts is not available.");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/info")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public ReportInfos getReportsInfo(@QueryParam("category") String category,
                                      @QueryParam("subCategory") String subCategory,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @Context SecurityContext securityContext){

        User user = (User) securityContext.getUserPrincipal();
        String found = yellowfinService.getUser(user.getName()).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

        if(found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(user.getName()).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));
        }

        if(found.equals("SUCCESS")) {
            ReportInfos reportInfos = new ReportInfos();
            reportInfos.addAll(
                    yellowfinService.getUserReports(user.getName(), category, subCategory, reportUUID).
                            orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build())));

            return reportInfos;
        }
        else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build());
        }
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/filter")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public FilterInfos getFiltersInfo(@QueryParam("reportId") int reportId,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @QueryParam("listAll") boolean listAll,
                                      @Context SecurityContext securityContext){
        FilterInfos filterInfos = new FilterInfos();
        User user = (User) securityContext.getUserPrincipal();
        String found = yellowfinService.getUser(user.getName()).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

        if(found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(user.getName()).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));
        }

        if(found.equals("SUCCESS")) {
            if (reportId != 0) {
                filterInfos.addAll(
                        yellowfinService.getReportFilters(reportId).orElseThrow(() -> new WebApplicationException(this.errorMessage, Response.Status.SERVICE_UNAVAILABLE)));
            } else {
                if (reportUUID != null) {
                    ReportInfos reportInfos = new ReportInfos();
                    reportInfos.addAll(
                            yellowfinService.getUserReports(user.getName(), null, null, reportUUID).
                                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build())));
                    if (reportInfos.total > 0) {
                        List<ReportInfo> reportInfo = reportInfos.reports;
                        filterInfos.addAll(
                                yellowfinService.getReportFilters(reportInfo.get(0).getReportId()).
                                        orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build())));
                    }
                }
            }
            return filterInfos;
        }
        else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build());
        }
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Path("/filterlistitems")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public FilterListItemInfos getFiltersInfo(@QueryParam("reportId") int reportId,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @QueryParam("filterId") String filterId,
                                      @Context SecurityContext securityContext){
        FilterListItemInfos filterInfos = new FilterListItemInfos();
        User user = (User) securityContext.getUserPrincipal();
        String found = yellowfinService.getUser(user.getName()).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));

        if(found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(user.getName()).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build()));
        }

        if(found.equals("SUCCESS")) {
            if (reportId != 0) {
                filterInfos.addAll(
                        yellowfinService.getFilterListItems(filterId, reportId).orElseThrow(() -> new WebApplicationException(this.errorMessage, Response.Status.SERVICE_UNAVAILABLE)));
            } else {
                if (reportUUID != null) {
                    ReportInfos reportInfos = new ReportInfos();
                    reportInfos.addAll(
                            yellowfinService.getUserReports(user.getName(), null, null, reportUUID).
                                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build())));
                    if (reportInfos.total > 0) {
                        List<ReportInfo> reportInfo = new ArrayList<>();
                        reportInfo = reportInfos.reports;
                        filterInfos.addAll(
                                yellowfinService.getFilterListItems(filterId, reportInfo.get(0).getReportId()).
                                        orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build())));
                    }
                }
            }
            return filterInfos;
        }
        else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(this.errorMessage).build());
        }
    }
}
