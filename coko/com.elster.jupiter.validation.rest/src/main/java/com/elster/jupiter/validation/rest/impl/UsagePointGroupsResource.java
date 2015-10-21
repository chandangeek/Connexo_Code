package com.elster.jupiter.validation.rest.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.QueryParameters;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.validation.rest.UsagePointGroupInfo;

@Path("/usagepointgroups")
public class UsagePointGroupsResource {

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;

    @Inject
    public UsagePointGroupsResource(MeteringGroupsService meteringGroupsService, RestQueryService restQueryService) {
        this.meteringGroupsService = meteringGroupsService;
        this.restQueryService = restQueryService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUsagePointGroups(@Context UriInfo uriInfo, @BeanParam JsonQueryParameters params) {
        QueryParameters queryParameters = QueryParameters.wrap(uriInfo.getQueryParameters());
        List<UsagePointGroupInfo> infos = queryUsagePointGroups(queryParameters).stream().map(UsagePointGroupInfo::new).collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromPagedList("usagepointgroups", infos, params)).build();
    }

    private List<UsagePointGroup> queryUsagePointGroups(QueryParameters queryParameters) {
        Query<UsagePointGroup> query = meteringGroupsService.getUsagePointGroupQuery();
        RestQuery<UsagePointGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }
}
