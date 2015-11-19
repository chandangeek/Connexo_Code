package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.search.SearchBuilder;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.search.SearchablePropertyValue;
import com.elster.jupiter.search.rest.SearchablePropertyValueConverter;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.rest.util.Transactional;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import com.google.common.collect.Range;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;

@Path("/devicegroups")
public class DeviceGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final SearchService searchService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceGroupInfoFactory deviceGroupInfoFactory;
    private final ResourceHelper resourceHelper;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, MeteringService meteringService, RestQueryService restQueryService, DeviceService deviceService, SearchService searchService, ExceptionFactory exceptionFactory, DeviceGroupInfoFactory deviceGroupInfoFactory, ResourceHelper resourceHelper) {
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
        this.restQueryService = restQueryService;
        this.deviceService = deviceService;
        this.searchService = searchService;
        this.exceptionFactory = exceptionFactory;
        this.deviceGroupInfoFactory = deviceGroupInfoFactory;
        this.resourceHelper = resourceHelper;
    }

    @GET @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters, @QueryParam("type") String typeName, @Context UriInfo uriInfo) {
        com.elster.jupiter.rest.util.QueryParameters koreQueryParameters = com.elster.jupiter.rest.util.QueryParameters.wrap(uriInfo.getQueryParameters());
        Query<EndDeviceGroup> query;
        if (QueryEndDeviceGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            query = meteringGroupsService.getQueryEndDeviceGroupQuery();
        } else {
            query = meteringGroupsService.getEndDeviceGroupQuery();
        }
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        List<EndDeviceGroup> allDeviceGroups = restQuery.select(koreQueryParameters, Order.ascending("upper(name)"));
        List<DeviceGroupInfo> deviceGroupInfos = deviceGroupInfoFactory.from(allDeviceGroups);
        return PagedInfoList.fromPagedList("devicegroups", deviceGroupInfos, queryParameters);
    }

    @GET @Transactional
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public DeviceGroupInfo getDeviceGroup(@PathParam("id") long id) {
        return deviceGroupInfoFactory.from(resourceHelper.findEndDeviceGroupOrThrowException(id));
    }

    @POST @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP)
    public Response createDeviceGroup(DeviceGroupInfo deviceGroupInfo) {
        if (meteringGroupsService.findEndDeviceGroupByName(deviceGroupInfo.name).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.DEVICEGROUPNAME_ALREADY_EXISTS, deviceGroupInfo.name);
        }
        EndDeviceGroup endDeviceGroup;
        if (deviceGroupInfo.dynamic) {
            SearchDomain deviceSearchDomain = findDeviceSearchDomainOrThrowException();
            JsonQueryFilter searchFilter = new JsonQueryFilter(deviceGroupInfo.filter);
            if (!searchFilter.hasFilters()) {
                throw exceptionFactory.newException(MessageSeeds.AT_LEAST_ONE_SEARCH_CRITERIA);
            }
            endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(buildSearchablePropertyConditions(deviceGroupInfo))
                    .setName(deviceGroupInfo.name)
                    .setSearchDomain(deviceSearchDomain)
                    .setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider")
                    .setLabel("MDC")
                    .setMRID("MDC:" + deviceGroupInfo.name)
                    .create();
        } else {
            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(buildListOfEndDevices(deviceGroupInfo))
                    .setName(deviceGroupInfo.name)
                    .setLabel("MDC")
                    .setMRID("MDC:" + deviceGroupInfo.name)
                    .create();
        }
        return Response.status(Response.Status.CREATED).entity(deviceGroupInfoFactory.from(endDeviceGroup)).build();
    }

    @PUT @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    @Path("/{id}")
    public Response editDeviceGroup(DeviceGroupInfo info, @PathParam("id") long id) {
        info.id = id;
        EndDeviceGroup endDeviceGroup = resourceHelper.lockEndDeviceGroupOrThrowException(info);
        endDeviceGroup.setName(info.name);
        endDeviceGroup.setMRID("MDC:" + info.name);

        if (info.dynamic) {
            ((QueryEndDeviceGroup) endDeviceGroup).setConditions(Arrays.asList(buildSearchablePropertyConditions(info)));
        } else {
            syncListWithInfo((EnumeratedEndDeviceGroup) endDeviceGroup, info);
        }
        endDeviceGroup.update();
        return Response.ok().entity(deviceGroupInfoFactory.from(endDeviceGroup)).build();
    }

    @DELETE @Transactional
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP)
    @Path("/{id}")
    public Response removeDeviceGroup(@PathParam("id") long id, DeviceGroupInfo info) {
        info.id = id;
        EndDeviceGroup endDeviceGroup = resourceHelper.lockEndDeviceGroupOrThrowException(info);
        endDeviceGroup.delete();
        return Response.ok().build();
    }

    @GET @Transactional
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public PagedInfoList getDevicesOfStaticDeviceGroup(@PathParam("id") long deviceGroupId, @BeanParam JsonQueryParameters queryParameters) {
        EndDeviceGroup endDeviceGroup = resourceHelper.findEndDeviceGroupOrThrowException(deviceGroupId);
        List<Device> devices = Collections.emptyList();
        if (endDeviceGroup.isDynamic()) {
            Finder<?> finder = findDeviceSearchDomainOrThrowException().finderFor(((QueryEndDeviceGroup) endDeviceGroup).getSearchablePropertyConditions());
            devices = finder.from(queryParameters).find().stream().map(Device.class::cast).collect(Collectors.toList());
        } else {
            List<? extends EndDevice> endDevices;
            if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
                endDevices = endDeviceGroup.getMembers(Instant.now(), queryParameters.getStart().get(), queryParameters.getLimit().get());
            } else {
                endDevices = endDeviceGroup.getMembers(Instant.now());
            }
            if (!endDevices.isEmpty()) {
                Condition mdcMembers = where("id").in(endDevices.stream().map(EndDevice::getAmrId).collect(toList()));
                devices = deviceService.findAllDevices(mdcMembers).sorted("mRID", true).stream().collect(toList());
            }
        }
        return PagedInfoList.fromPagedList("devices", DeviceGroupMemberInfo.from(devices), queryParameters);
    }

    private SearchablePropertyValue[] buildSearchablePropertyConditions(DeviceGroupInfo deviceGroupInfo) {
        SearchDomain searchDomain = findDeviceSearchDomainOrThrowException();
        JsonQueryFilter filter = new JsonQueryFilter(deviceGroupInfo.filter);
        if (!filter.hasFilters()) {
            throw exceptionFactory.newException(MessageSeeds.AT_LEAST_ONE_SEARCH_CRITERIA);
        }
        return searchDomain.getPropertiesValues(property -> SearchablePropertyValueConverter.convert(property, filter))
                .stream().toArray(SearchablePropertyValue[]::new);
    }

    private EndDevice[] buildListOfEndDevices(DeviceGroupInfo deviceGroupInfo) {
        Stream<Long> deviceIds;
        if (deviceGroupInfo.devices == null) {//static device group with ALL option
            SearchBuilder<Device> searchBuilder = searchService.search(Device.class);
            Stream.of(buildSearchablePropertyConditions(deviceGroupInfo)).forEach(searchablePropertyValue -> {
                try {
                    searchablePropertyValue.addAsCondition(searchBuilder);
                } catch (InvalidValueException e) {
                    throw new LocalizedFieldValidationException(MessageSeeds.SEARCHABLE_PROPERTY_INVALID_VALUE, "filter." + searchablePropertyValue.getValueBean().propertyName);
                }
            });
            deviceIds = searchBuilder.toFinder().stream().map(HasId::getId);
        } else {
            deviceIds = deviceGroupInfo.devices.stream();
        }
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        return deviceIds.map(number -> amrSystem.findMeter(number.toString()))
                .flatMap(asStream())
                .toArray(EndDevice[]::new);
    }

    private void syncListWithInfo(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, DeviceGroupInfo deviceGroupInfo) {
        EndDevice[] endDevices = buildListOfEndDevices(deviceGroupInfo);
        Map<Long, EnumeratedEndDeviceGroup.Entry> currentEntries = enumeratedEndDeviceGroup.getEntries().stream().collect(toMap());
        // remove those no longer mapped
        currentEntries.entrySet().stream()
                .filter(entry -> Stream.of(endDevices).mapToLong(EndDevice::getId).noneMatch(id -> id == entry.getKey()))
                .forEach(entry -> enumeratedEndDeviceGroup.remove(entry.getValue()));
        // add new ones
        Stream.of(endDevices)
                .filter(device -> !currentEntries.containsKey(device.getId()))
                .forEach(device -> enumeratedEndDeviceGroup.add(device, Range.atLeast(Instant.EPOCH)));
    }

    private Collector<EnumeratedEndDeviceGroup.Entry, ?, Map<Long, EnumeratedEndDeviceGroup.Entry>> toMap() {
        return Collectors.toMap(entry -> entry.getEndDevice().getId(), Function.identity());
    }

    private SearchDomain findDeviceSearchDomainOrThrowException() {
        return searchService.findDomain(Device.class.getName()).
                orElseThrow(() -> exceptionFactory.newException(MessageSeeds.DEVICE_SEARCH_DOMAIN_NOT_REGISTERED));
    }
}