/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.IdentifiedObject;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.mdm.usagepoint.data.security.Privileges;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EnumeratedGroup;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.GroupBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.rest.util.Transactional;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;

@Path("/usagepointgroups")
public class UsagePointGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final SearchService searchService;
    private final ExceptionFactory exceptionFactory;
    private final UsagePointGroupInfoFactory usagePointGroupInfoFactory;
    private final UsagePointInfoFactory usagePointInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public UsagePointGroupResource(MeteringGroupsService meteringGroupsService, MeteringService meteringService,
                                   SearchService searchService, ExceptionFactory exceptionFactory,
                                   UsagePointGroupInfoFactory usagePointGroupInfoFactory,
                                   UsagePointInfoFactory usagePointInfoFactory, ResourceHelper resourceHelper) {
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.searchService = searchService;
        this.exceptionFactory = exceptionFactory;
        this.usagePointGroupInfoFactory = usagePointGroupInfoFactory;
        this.usagePointInfoFactory = usagePointInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
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
        return PagedInfoList.fromPagedList("usagePointGroups", usagePointGroupInfoList, queryParameters);
    }

    @GET
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public UsagePointGroupInfo getUsagePointGroup(@PathParam("id") long id) {
        return usagePointGroupInfoFactory.from(resourceHelper.findUsagePointGroupOrThrowException(id));
    }

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP)
    public Response createUsagePointGroup(UsagePointGroupInfo usagePointGroupInfo,
                                          @QueryParam("validate") @DefaultValue("false") boolean onlyValidate) {
        new RestValidationBuilder()
                .notEmpty(usagePointGroupInfo.name, "name")
                .notEmpty(usagePointGroupInfo.dynamic, "groupType")
                .validate();
        UsagePointGroup usagePointGroup;
        if (usagePointGroupInfo.dynamic) {
            usagePointGroup = buildQueryUsagePointGroup(usagePointGroupInfo, onlyValidate);
        } else {
            usagePointGroup = buildEnumeratedUsagePointGroup(usagePointGroupInfo, onlyValidate);
        }
        return onlyValidate ?
                Response.accepted().build() :
                Response.status(Response.Status.CREATED).entity(usagePointGroupInfoFactory.from(usagePointGroup)).build();
    }

    @PUT
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public Response editUsagePointGroup(UsagePointGroupInfo info, @PathParam("id") long id) {
        info.id = id;
        UsagePointGroup usagePointGroup = resourceHelper.lockUsagePointGroupOrThrowException(info);
        usagePointGroup.setName(info.name);
        usagePointGroup.setMRID("MDM:" + info.name);

        if (info.dynamic) {
            ((QueryUsagePointGroup) usagePointGroup).setConditions(Arrays.asList(buildSearchablePropertyConditions(info)));
        } else {
            syncListWithInfo((EnumeratedUsagePointGroup) usagePointGroup, info);
        }
        usagePointGroup.update();
        return Response.ok().entity(usagePointGroupInfoFactory.from(usagePointGroup)).build();
    }

    @DELETE
    @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP)
    public Response removeUsagePointGroup(@PathParam("id") long id, UsagePointGroupInfo info) {
        info.id = id;
        resourceHelper.lockUsagePointGroupOrThrowException(info)
                .delete();
        return Response.ok().build();
    }

    @GET
    @Transactional
    @Path("/{id}/usagepoints")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public PagedInfoList getUsagePointsInGroup(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        UsagePointGroup usagePointGroup = resourceHelper.findUsagePointGroupOrThrowException(id);
        List<UsagePoint> usagePoints;
        if (usagePointGroup.isDynamic()) {
            usagePoints = fetchMembersOfQueryUsagePointGroup((QueryUsagePointGroup) usagePointGroup, queryParameters);
        } else {
            usagePoints = fetchMembersOfEnumUsagePointGroup((EnumeratedUsagePointGroup) usagePointGroup, queryParameters);
        }
        return PagedInfoList.fromPagedList("usagePoints", usagePointInfoFactory.from(usagePoints), queryParameters);
    }

    @GET
    @Transactional
    @Path("/{id}/usagepoints/count")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTER_USAGE_POINT_GROUP,
            Privileges.Constants.ADMINISTER_USAGE_POINT_ENUMERATED_GROUP,
            Privileges.Constants.VIEW_USAGE_POINT_GROUP_DETAIL})
    public Response getUsagePointsCountInGroup(@PathParam("id") long id, @BeanParam JsonQueryParameters queryParameters) {
        long numberOfSearchResults = resourceHelper.findUsagePointGroupOrThrowException(id)
                .getMemberCount(Instant.now());
        return Response.ok().entity(ImmutableMap.of("numberOfSearchResults", numberOfSearchResults)).build();
    }

    private Query<UsagePointGroup> getUsagePointGroupQueryByType(@QueryParam("type") String typeName) {
        if (QueryUsagePointGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            return meteringGroupsService.getQueryUsagePointGroupQuery();
        } else {
            return meteringGroupsService.getUsagePointGroupQuery();
        }
    }

    private static Condition buildCondition(JsonQueryFilter filter) {
        return filter.hasProperty("name") ?
                where("name").isEqualTo(filter.getString("name")) :
                Condition.TRUE;
    }

    private EnumeratedUsagePointGroup buildEnumeratedUsagePointGroup(UsagePointGroupInfo usagePointGroupInfo,
                                                                     boolean onlyValidate) {
        GroupBuilder.GroupCreator<? extends EnumeratedUsagePointGroup> creator = meteringGroupsService
                .createEnumeratedUsagePointGroup(onlyValidate ?
                        new UsagePoint[0] :
                        buildListOfUsagePoints(usagePointGroupInfo))
                .setName(usagePointGroupInfo.name)
                .setLabel("MDM")
                .setMRID("MDM:" + usagePointGroupInfo.name);
        return onlyValidate ? creator.validate() : creator.create();
    }

    private QueryUsagePointGroup buildQueryUsagePointGroup(UsagePointGroupInfo usagePointGroupInfo,
                                                           boolean onlyValidate) {
        SearchDomain usagePointSearchDomain = findUsagePointSearchDomainOrThrowException();
        GroupBuilder.GroupCreator<? extends QueryUsagePointGroup> creator = meteringGroupsService
                .createQueryUsagePointGroup(onlyValidate ?
                        new SearchablePropertyValue[0] :
                        buildSearchablePropertyConditions(usagePointGroupInfo))
                .setName(usagePointGroupInfo.name)
                .setSearchDomain(usagePointSearchDomain)
                .setQueryProviderName("com.elster.jupiter.metering.groups.impl.SimpleUsagePointQueryProvider")
                .setLabel("MDM")
                .setMRID("MDM:" + usagePointGroupInfo.name);
        return onlyValidate ? creator.validate() : creator.create();
    }

    private UsagePoint[] buildListOfUsagePoints(UsagePointGroupInfo usagePointGroupInfo) {
        Stream<Long> usagePointIds;
        if (usagePointGroupInfo.usagePoints == null) {//static usage point group with ALL option
            SearchBuilder<UsagePoint> searchBuilder = searchService.search(UsagePoint.class);
            Stream.of(buildSearchablePropertyConditions(usagePointGroupInfo)).forEach(searchablePropertyValue -> {
                try {
                    searchablePropertyValue.addAsCondition(searchBuilder);
                } catch (InvalidValueException e) {
                    throw new LocalizedFieldValidationException(MessageSeeds.SEARCHABLE_PROPERTY_INVALID_VALUE,
                            "filter." + searchablePropertyValue.getValueBean().propertyName);
                }
            });
            usagePointIds = searchBuilder.toFinder().stream().map(UsagePoint::getId);
        } else {
            usagePointIds = usagePointGroupInfo.usagePoints.stream();
        }
        return usagePointIds.map(meteringService::findUsagePointById)
                .flatMap(asStream())
                .toArray(UsagePoint[]::new);
    }

    private SearchDomain findUsagePointSearchDomainOrThrowException() {
        return searchService.findDomain(UsagePoint.class.getName()).
                orElseThrow(() -> exceptionFactory.newException(MessageSeeds.USAGE_POINT_SEARCH_DOMAIN_NOT_REGISTERED));
    }

    private SearchablePropertyValue[] buildSearchablePropertyConditions(UsagePointGroupInfo usagePointGroupInfo) {
        SearchDomain searchDomain = findUsagePointSearchDomainOrThrowException();
        JsonQueryFilter filter = new JsonQueryFilter(usagePointGroupInfo.filter);
        if (!filter.hasFilters()) {
            throw exceptionFactory.newException(MessageSeeds.AT_LEAST_ONE_SEARCH_CRITERION);
        }
        return searchDomain.getPropertiesValues(property -> SearchablePropertyValueConverter.convert(property, filter))
                .stream().toArray(SearchablePropertyValue[]::new);
    }

    private void syncListWithInfo(EnumeratedUsagePointGroup enumeratedUsagePointGroup,
                                  UsagePointGroupInfo usagePointGroupInfo) {
        UsagePoint[] usagePoints = buildListOfUsagePoints(usagePointGroupInfo);
        Map<Long, EnumeratedGroup.Entry<UsagePoint>> currentEntries = enumeratedUsagePointGroup.getEntries().stream()
                .collect(indexedById());
        // remove those no longer mapped
        currentEntries.entrySet().stream()
                .filter(entry -> Arrays.stream(usagePoints)
                        .mapToLong(UsagePoint::getId)
                        .noneMatch(id -> id == entry.getKey()))
                .forEach(entry -> enumeratedUsagePointGroup.remove(entry.getValue()));
        // add new ones
        Arrays.stream(usagePoints)
                .filter(usagePoint -> !currentEntries.containsKey(usagePoint.getId()))
                .forEach(usagePoint -> enumeratedUsagePointGroup.add(usagePoint, Range.atLeast(Instant.EPOCH)));
    }

    private static <T extends HasId & IdentifiedObject>
    Collector<EnumeratedGroup.Entry<T>, ?, Map<Long, EnumeratedGroup.Entry<T>>> indexedById() {
        return Collectors.toMap(entry -> entry.getMember().getId(), Function.identity());
    }

    private static List<UsagePoint> fetchMembersOfEnumUsagePointGroup(EnumeratedUsagePointGroup usagePointGroup,
                                                               JsonQueryParameters queryParameters) {
        Optional<Integer> start = queryParameters.getStart();
        Optional<Integer> limit = queryParameters.getLimit();
        return start.isPresent() && limit.isPresent() ?
                usagePointGroup.getMembers(Instant.now(), start.get(), limit.get()) :
                usagePointGroup.getMembers(Instant.now());
    }

    private List<UsagePoint> fetchMembersOfQueryUsagePointGroup(QueryUsagePointGroup usagePointGroup,
                                                                JsonQueryParameters queryParameters) {
        return findUsagePointSearchDomainOrThrowException()
                .finderFor(usagePointGroup.getSearchablePropertyConditions())
                .from(queryParameters)
                .stream()
                .map(UsagePoint.class::cast)
                .collect(toList());
    }
}
