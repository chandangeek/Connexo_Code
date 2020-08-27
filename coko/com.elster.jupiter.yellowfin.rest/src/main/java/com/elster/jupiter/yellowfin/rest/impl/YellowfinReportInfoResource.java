/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.MessageSeeds;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/report")
public class YellowfinReportInfoResource {

    private YellowfinService yellowfinService;
    private Thesaurus thesaurus;


    @Inject
    private YellowfinReportInfoResource(YellowfinService yellowfinService, Thesaurus thesaurus) {
        this.yellowfinService = yellowfinService;
        this.thesaurus = thesaurus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/info")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public ReportInfos getReportsInfo(@QueryParam("category") String category,
                                      @QueryParam("subCategory") String subCategory,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @Context SecurityContext securityContext) {

        String userName = getName((User) securityContext.getUserPrincipal());
        String found = yellowfinService.getUser(userName).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

        if (found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));
        }

        if (found.equals("SUCCESS")) {
            ReportInfos reportInfos = new ReportInfos();
            reportInfos.addAll(
                    yellowfinService.getUserReports(userName, category, subCategory, reportUUID).
                            orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build())));

            return reportInfos;
        } else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE_ENHANCED).format(found)).build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/filter")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public FilterInfos getFiltersInfo(@QueryParam("reportId") int reportId,
                                      @QueryParam("reportUUID") String reportUUID,
                                      @QueryParam("listAll") boolean listAll,
                                      @Context SecurityContext securityContext) {
        FilterInfos filterInfos = new FilterInfos();
        String userName = getName((User) securityContext.getUserPrincipal());
        String found = yellowfinService.getUser(userName).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

        if (found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));
        }

        if (found.equals("SUCCESS")) {
            if (reportId != 0) {
                filterInfos.addAll(
                        yellowfinService.getReportFilters(reportId).orElseThrow(() -> new WebApplicationException(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format(), Response.Status.SERVICE_UNAVAILABLE)));
            } else {
                if (reportUUID != null) {
                    ReportInfos reportInfos = new ReportInfos();
                    reportInfos.addAll(
                            yellowfinService.getUserReports(userName, null, null, reportUUID).
                                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build())));
                    if (reportInfos.total > 0) {
                        List<ReportInfo> reportInfo = reportInfos.reports;
                        filterInfos.addAll(
                                yellowfinService.getReportFilters(reportInfo.get(0).getReportId()).
                                        orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build())));
                    }
                }
            }
            return filterInfos;
        } else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE_ENHANCED).format(found)).build());
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Path("/filterlistitems")
    @RolesAllowed(Privileges.Constants.VIEW_REPORTS)
    public FilterListItemInfos getFiltersInfo(@QueryParam("reportId") int reportId,
                                              @QueryParam("reportUUID") String reportUUID,
                                              @QueryParam("filterId") String filterId,
                                              @Context SecurityContext securityContext) {
        FilterListItemInfos filterInfos = new FilterListItemInfos();
        String userName = getName((User) securityContext.getUserPrincipal());
        String found = yellowfinService.getUser(userName).
                orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));

        if (found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(userName).
                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build()));
        }

        if (found.equals("SUCCESS")) {
            if (reportId != 0) {
                filterInfos.addAll(
                        yellowfinService.getFilterListItems(filterId, reportId).orElseThrow(() -> new WebApplicationException(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format(), Response.Status.SERVICE_UNAVAILABLE)));
            } else {
                if (reportUUID != null) {
                    ReportInfos reportInfos = new ReportInfos();
                    reportInfos.addAll(
                            yellowfinService.getUserReports(userName, null, null, reportUUID).
                                    orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build())));
                    if (reportInfos.total > 0) {
                        List<ReportInfo> reportInfo = new ArrayList<>();
                        reportInfo = reportInfos.reports;
                        filterInfos.addAll(
                                yellowfinService.getFilterListItems(filterId, reportInfo.get(0).getReportId()).
                                        orElseThrow(() -> new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE).format()).build())));
                    }
                }
            }
            return filterInfos;
        } else {
            throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(thesaurus.getFormat(MessageSeeds.FACTS_NOT_AVAILABLE_ENHANCED).format(found)).build());
        }
    }

    private String getName(User user) {
        String userName = user.getName();
        if (! user.getPrivileges("YFN").stream().anyMatch(p -> p.getName().equals("privilege.design.reports") || p.getName().equals("privilege.administrate.reports"))) {
            userName = YellowfinResource.USER_REPORTS_VIEWER;
        }
        return userName;
    }
}
