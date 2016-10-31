package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.mdm.usagepoint.data.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/usagepointgroups")
public class UsagePointGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final SearchService searchService;
    private final ExceptionFactory exceptionFactory;
    private final UsagePointGroupInfoFactory usagePointGroupInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointGroupResource(MeteringGroupsService meteringGroupsService, MeteringService meteringService,
                                   SearchService searchService, ExceptionFactory exceptionFactory,
                                   UsagePointGroupInfoFactory usagePointGroupInfoFactory, ResourceHelper resourceHelper) {
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.searchService = searchService;
        this.exceptionFactory = exceptionFactory;
        this.usagePointGroupInfoFactory = usagePointGroupInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getUsagePointGroups(@QueryParam("type") String typeName, @BeanParam JsonQueryFilter filter,
                                             @BeanParam JsonQueryParameters queryParameters) {
        Query<UsagePointGroup> query = getUsagePointGroupQueryByType(typeName);
        Condition condition = buildCondition(filter);
        Order order = Order.ascending("upper(name)");
        List<UsagePointGroup> usagePointGroups;
        Optional<Integer> start = queryParameters.getStart();
        Optional<Integer> limit = queryParameters.getLimit();
        if (start.isPresent() && limit.isPresent()) {
            int from = start.get() + 1;
            usagePointGroups = query.select(condition, from, from + limit.get(), order);
        } else {
            usagePointGroups = query.select(condition, order);
        }
        List<UsagePointGroupInfo> usagePointGroupInfoList = usagePointGroupInfoFactory.from(usagePointGroups);
        return PagedInfoList.fromPagedList("usagepointgroups", usagePointGroupInfoList, queryParameters);
    }

    private Query<UsagePointGroup> getUsagePointGroupQueryByType(@QueryParam("type") String typeName) {
        if (QueryUsagePointGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            return meteringGroupsService.getQueryUsagePointGroupQuery();
        } else {
            return meteringGroupsService.getUsagePointGroupQuery();
        }
    }

    private Condition buildCondition(JsonQueryFilter filter) {
        return filter.hasProperty("name") ?
                where("name").isEqualTo(filter.getString("name")) :
                Condition.TRUE;
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_USAGE_POINT_GROUP,
            Privileges.Constants.ADMINISTRATE_USAGE_POINT_ENUMERATED_GROUP,
            Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public UsagePointGroupInfo getUsagePointGroup(@PathParam("id") long id) {
        return usagePointGroupInfoFactory.from(resourceHelper.findUsagePointGroupOrThrowException(id));
    }

}
