/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.impl.resource;

import com.elster.jupiter.issue.rest.resource.StandardParametersBean;
import com.elster.jupiter.issue.rest.response.ResponseHelper;
import com.elster.jupiter.issue.rest.response.device.MeterShortInfo;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterFilter;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static com.elster.jupiter.issue.rest.response.ResponseHelper.entity;

@Path("/meters")
public class MeterResource extends BaseResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getMeters(@BeanParam StandardParametersBean params) {
        validateMandatory(params, START, LIMIT);
        String searchText = params.getFirst(LIKE);
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? "*" + searchText + "*" : "*";
        MeterFilter filter = new MeterFilter();
        filter.setName(dbSearchText);
        String lowerCaseSearchText = searchText == null ? "" : searchText.toLowerCase();
        List<Meter> listMeters = getMeteringService().findMeters(filter)
                .paged(params.getStart(), params.getLimit())
                .sorted(Order.ascending("name").apply("LENGTH"))
                .sorted(Order.ascending("name")
                        .applySqlString("(CASE WHEN LOWER(name) LIKE '%" + lowerCaseSearchText +
                                "%' THEN INSTR(LOWER(name), '" + lowerCaseSearchText +
                                "' ) ELSE 9999 END)"))  // 9999 - means some bigger than max character position in record
                .sorted("name", true)
                .stream()
                .collect(Collectors.toList());
        return entity(listMeters, MeterShortInfo.class, params.getStart(), params.getLimit()).build();
    }

    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_ISSUE, Privileges.Constants.ASSIGN_ISSUE, Privileges.Constants.CLOSE_ISSUE, Privileges.Constants.COMMENT_ISSUE, Privileges.Constants.ACTION_ISSUE})
    public Response getMeter(@PathParam("name") String name) {
        return getMeteringService().findMeterByName(name)
                .map(MeterShortInfo::new)
                .map(ResponseHelper::entity)
                .map(Response.ResponseBuilder::build)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }
}
