/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterFilter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQueryService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("/devices")
public class DeviceResource {

    private final RestQueryService queryService;
    private final MeteringService meteringService;

    @Inject
    public DeviceResource(RestQueryService queryService, MeteringService meteringService) {
        this.queryService = queryService;
        this.meteringService = meteringService;
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevices(@Context UriInfo uriInfo, @Context SecurityContext securityContext) {
        QueryParameters params = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<Meter> list = queryDevices(maySeeAny(securityContext), params);
        return toMeterInfos(params.clipToLimit(list), params.getStartInt(), params.getLimit());
    }

    private MeterInfos toMeterInfos(List<Meter> list, int start, int limit) {
        MeterInfos infos = new MeterInfos(list);
        infos.total = start + list.size();
        if (list.size() == limit) {
            infos.total++;
        }
        return infos;
    }

    private boolean maySeeAny(SecurityContext securityContext) {
        return securityContext.isUserInRole(Privileges.Constants.VIEW_ANY_USAGEPOINT);
    }

    private List<Meter> queryDevices(boolean maySeeAny, QueryParameters queryParameters) {
        Query<Meter> query = meteringService.getMeterQuery();
        if (!maySeeAny) {
            query.setRestriction(meteringService.hasAccountability());
        }
        return queryService.wrap(query).select(queryParameters);
    }

    @GET
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public Response getAvailableMeters(@BeanParam JsonQueryParameters params) {
        String searchText = params.getLike();
        Integer start = params.getStart().orElse(1);
        Integer limit = params.getLimit().orElse(50);
        String dbSearchText = (searchText != null && !searchText.isEmpty()) ? ("*" + searchText + "*") : "*";
        MeterFilter filter = new MeterFilter();
        filter.setName(dbSearchText);
        filter.setExcludedStates("dlc.default.decommissioned", "dlc.default.removed");
        Finder<Meter> finder = meteringService.findMeters(filter);
        return Response.ok().entity(toMeterInfos(params.getStart().isPresent()
                && params.getLimit().isPresent() ? finder.from(params).find() : finder.paged(start, limit).find(), start, limit)).build();
    }

    @GET
    @RolesAllowed({Privileges.Constants.VIEW_ANY_USAGEPOINT, Privileges.Constants.VIEW_OWN_USAGEPOINT})
    @Path("/{name}/")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public MeterInfos getDevice(@PathParam("name") String name, @Context SecurityContext securityContext) {
        if (maySeeAny(securityContext)) {
            return meteringService.findMeterByName(name)
                    .map(MeterInfos::new)
                    .orElse(null);
        }
        return null;
    }
}
