package com.elster.jupiter.export.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Order;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 6/11/2014
 * Time: 12:03
 */
@Path("/metergroups")
public class MeterGroupsResource {

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;

    @Inject
    public MeterGroupsResource(MeteringGroupsService meteringGroupsService, RestQueryService restQueryService) {
        this.meteringGroupsService = meteringGroupsService;
        this.restQueryService = restQueryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceGroups(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters parameters) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<EndDeviceGroup> allDeviceGroups = queryEndDeviceGroups(queryParameters);
        return PagedInfoList.fromPagedList("metergroups",
                allDeviceGroups.stream()
                        .map(endDeviceGroup -> new IdWithNameInfo(endDeviceGroup.getId(), endDeviceGroup.getName()))
                        .collect(Collectors.toList()), parameters);
    }

    private List<EndDeviceGroup> queryEndDeviceGroups(QueryParameters queryParameters) {
        Query<EndDeviceGroup> query = meteringGroupsService.getEndDeviceGroupQuery();
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }
}
