/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.yellowfin.rest.impl;


import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinService;
import com.elster.jupiter.yellowfin.MessageSeeds;
import com.elster.jupiter.yellowfin.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.util.List;

@Path("/report")
public class YellowfinReportInfoResource {

    private YellowfinService yellowfinService;
    private Thesaurus thesaurus;


    @Inject
    private YellowfinReportInfoResource(YellowfinService yellowfinService, Thesaurus thesaurus){
        this.yellowfinService = yellowfinService;
        this.thesaurus = thesaurus;
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
                orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){});

        if(found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(user.getName()).
                    orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){});
        }

        if(found.equals("SUCCESS")) {
            ReportInfos reportInfos = new ReportInfos();
            reportInfos.addAll(
                    yellowfinService.getUserReports(user.getName(), category, subCategory, reportUUID).
                            orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));

            return reportInfos;
        }
        else {
            throw new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){};
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
                orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){});

        if(found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(user.getName()).
                    orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){});
        }

        if(found.equals("SUCCESS")) {
            if (reportId != 0) {
                filterInfos.addAll(
                        yellowfinService.getReportFilters(reportId).orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));
            } else {
                if (reportUUID != null) {
                    ReportInfos reportInfos = new ReportInfos();
                    reportInfos.addAll(
                            yellowfinService.getUserReports(user.getName(), null, null, reportUUID).
                                    orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));
                    if (reportInfos.total > 0) {
                        List<ReportInfo> reportInfo = reportInfos.reports;
                        filterInfos.addAll(
                                yellowfinService.getReportFilters(reportInfo.get(0).getReportId()).
                                        orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));
                    }
                }
            }
            return filterInfos;
        }
        else {
            throw new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){};
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
                orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){});

        if(found.equals("NOT_FOUND")) {
            found = yellowfinService.createUser(user.getName()).
                    orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){});
        }

        if(found.equals("SUCCESS")) {
            if (reportId != 0) {
                filterInfos.addAll(
                        yellowfinService.getFilterListItems(filterId, reportId).orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));
            } else {
                if (reportUUID != null) {
                    ReportInfos reportInfos = new ReportInfos();
                    reportInfos.addAll(
                            yellowfinService.getUserReports(user.getName(), null, null, reportUUID).
                                    orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));
                    if (reportInfos.total > 0) {
                        List<ReportInfo> reportInfo;
                        reportInfo = reportInfos.reports;
                        filterInfos.addAll(
                                yellowfinService.getFilterListItems(filterId, reportInfo.get(0).getReportId()).
                                        orElseThrow(() -> new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){}));
                    }
                }
            }
            return filterInfos;
        }
        else {
            throw new LocalizedException(thesaurus, MessageSeeds.FACTS_NOT_AVAILABLE,YellowfinService.COMPONENTNAME){};
        }
    }
}
