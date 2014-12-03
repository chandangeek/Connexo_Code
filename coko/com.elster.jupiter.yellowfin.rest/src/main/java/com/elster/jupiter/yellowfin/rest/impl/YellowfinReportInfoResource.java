package com.elster.jupiter.yellowfin.rest.impl;


import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.yellowfin.YellowfinService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("/report")
public class YellowfinReportInfoResource {

    private YellowfinService yellowfinService;



    @Inject
    private YellowfinReportInfoResource(YellowfinService yellowfinService){
        this.yellowfinService = yellowfinService;
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/test")
    public String test(){

        return"C";

    }
    @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/info")
     public ReportInfos getReportsInfo(@QueryParam("category") String category,
                                       @QueryParam("subCategory") String subCategory,
                                       @Context SecurityContext securityContext){

        User user = (User) securityContext.getUserPrincipal();
        ReportInfos reportInfos = new ReportInfos();
        reportInfos.addAll(yellowfinService.getUserReports(user.getName(), category, subCategory));
        return reportInfos;

    }


}
