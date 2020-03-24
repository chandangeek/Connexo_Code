/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.resource;


import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ResponseHelper;
import com.elster.jupiter.issue.rest.response.device.MeterShortInfo;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterFilter;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.device.alarms.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/meters")
public class MeterResource extends BaseAlarmResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public Response getMeters(@BeanParam StandardParametersBean params) {
        String searchText = params.getFirst("like");
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? "*" + searchText + "*" : "*";
        MeterFilter filter = new MeterFilter();
        filter.setName(dbSearchText);
        List<Meter> listMeters = getMeteringService().findMeters(filter)
                .stream()
                .sorted(Comparator.comparingInt((Meter meter) -> meter.getName().length())
                        .thenComparingInt(meter -> meter.getName().toLowerCase().indexOf(searchText == null ? "" : searchText.toLowerCase()))
                        .thenComparing(HasName::getName))
                .collect(Collectors.toList());
        return entity(listMeters, MeterShortInfo.class, params.getStart(), params.getLimit()).build();
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ALARM, Privileges.Constants.ASSIGN_ALARM, Privileges.Constants.CLOSE_ALARM, Privileges.Constants.COMMENT_ALARM, Privileges.Constants.ACTION_ALARM})
    public Response getMeter(@PathParam("name") String name) {
        return getMeteringService().findMeterByName(name)
                .map(MeterShortInfo::new)
                .map(ResponseHelper::entity)
                .map(Response.ResponseBuilder::build)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

}
