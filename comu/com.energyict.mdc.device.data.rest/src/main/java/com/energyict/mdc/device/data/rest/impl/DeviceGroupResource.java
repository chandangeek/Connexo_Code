package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;
import static java.util.stream.Collectors.toList;

@Path("/devicegroups")
public class DeviceGroupResource {
    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;
    private final DeviceGroupInfoFactory deviceGroupInfoFactory;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, RestQueryService restQueryService, DeviceService deviceService, ExceptionFactory exceptionFactory, DeviceGroupInfoFactory deviceGroupInfoFactory) {
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.restQueryService = restQueryService;
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
        this.deviceGroupInfoFactory = deviceGroupInfoFactory;
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    public DeviceGroupInfo getDeviceGroup(@PathParam("id") long id) {
        return deviceGroupInfoFactory.from(fetchDeviceGroup(id));
    }

    @GET
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({ Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL })
    public PagedInfoList getDevices(@PathParam("id") long deviceGroupId, @BeanParam JsonQueryParameters queryParameters) {
        EndDeviceGroup endDeviceGroup = fetchDeviceGroup(deviceGroupId);
        List<? extends EndDevice> endDevices;
        if (queryParameters.getStart().isPresent() && queryParameters.getLimit().isPresent()) {
            endDevices= endDeviceGroup.getMembers(Instant.now(), queryParameters.getStart().get(), queryParameters.getLimit().get());
        } else {
            endDevices= endDeviceGroup.getMembers(Instant.now());
        }
        List<Device> devices = Collections.emptyList();
        if (!endDevices.isEmpty()) {
            Condition mdcMembers = where("id").in(endDevices.stream().map(EndDevice::getAmrId).collect(toList()));
            devices = deviceService.findAllDevices(mdcMembers).sorted("mRID", true).stream().collect(toList());
        }
        return PagedInfoList.fromPagedList("devices", DeviceGroupMemberInfo.from(devices), queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combo-box containing all the groups needs to be shown when creating an export task
    public PagedInfoList getDeviceGroups(@BeanParam JsonQueryParameters queryParameters, @QueryParam("type") String typeName, @Context UriInfo uriInfo) {

        com.elster.jupiter.rest.util.QueryParameters koreQueryParameters =
                com.elster.jupiter.rest.util.QueryParameters.wrap(uriInfo.getQueryParameters());
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

    @PUT
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.Constants.ADMINISTRATE_DEVICE_GROUP, Privileges.Constants.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.Constants.VIEW_DEVICE_GROUP_DETAIL})
    @Path("/{id}")
    public Response editDeviceGroup(DeviceGroupInfo deviceGroupInfo, @PathParam("id") long id) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        endDeviceGroup.setName(deviceGroupInfo.name);
        endDeviceGroup.setMRID("MDC:" + deviceGroupInfo.name);

        if (deviceGroupInfo.dynamic) {
            ((QueryEndDeviceGroup) endDeviceGroup).setCondition(getCondition(deviceGroupInfo));
        } else {
            syncListWithInfo((EnumeratedEndDeviceGroup) endDeviceGroup, deviceGroupInfo);
        }
        endDeviceGroup.save();
        return Response.status(Response.Status.CREATED).entity(deviceGroupInfoFactory.from(endDeviceGroup)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP)
    @Path("/{id}")
    public Response removeDeviceGroup(@PathParam("id") long id) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        endDeviceGroup.delete();
        return Response.ok().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_DEVICE_GROUP)
    public Response createDeviceGroup(DeviceGroupInfo deviceGroupInfo) {
        if (meteringGroupsService.findEndDeviceGroupByName(deviceGroupInfo.name).isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.DEVICEGROUPNAME_ALREADY_EXISTS, deviceGroupInfo.name);
        }
        String name = deviceGroupInfo.name;
        boolean dynamic = deviceGroupInfo.dynamic;

        EndDeviceGroup endDeviceGroup;
        if (dynamic) {
            endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(getCondition(deviceGroupInfo));
            endDeviceGroup.setName(name);
            endDeviceGroup.setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider");
        } else {
            EnumeratedEndDeviceGroup enumeratedEndDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(name);
            syncListWithInfo(enumeratedEndDeviceGroup, deviceGroupInfo);
            endDeviceGroup = enumeratedEndDeviceGroup;
        }
        endDeviceGroup.setLabel("MDC");
        endDeviceGroup.setMRID("MDC:" + endDeviceGroup.getName());
        endDeviceGroup.save();

        return Response.status(Response.Status.CREATED).entity(deviceGroupInfoFactory.from(endDeviceGroup)).build();
    }

    private EndDeviceGroup fetchDeviceGroup(long id) {
        return meteringGroupsService.findEndDeviceGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private void syncListWithInfo(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, DeviceGroupInfo deviceGroupInfo) {
        Stream<Long> deviceIds;
        if (deviceGroupInfo.devices == null) {
            deviceIds = deviceService
                    .findAllDevices(getCondition(deviceGroupInfo))
                    .stream()
                    .map(HasId::getId);
        }
        else {
            deviceIds = deviceGroupInfo.devices.stream();
        }
        AmrSystem amrSystem = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
        List<EndDevice> endDevices = deviceIds.map(number -> amrSystem.findMeter(number.toString()))
                .flatMap(asStream())
                .collect(toList());

        Map<Long, EnumeratedEndDeviceGroup.Entry> currentEntries = enumeratedEndDeviceGroup.getEntries().stream()
                .collect(toMap());

        // remove those no longer mapped
        currentEntries.entrySet().stream()
                .filter(entry -> endDevices.stream().mapToLong(EndDevice::getId).noneMatch(id -> id == entry.getKey()))
                .forEach(entry -> enumeratedEndDeviceGroup.remove(entry.getValue()));

        // add new ones
        endDevices.stream()
                .filter(device -> !currentEntries.containsKey(device.getId()))
                .forEach(device -> enumeratedEndDeviceGroup.add(device, Interval.sinceEpoch().toClosedRange()));
    }

    private Collector<EnumeratedEndDeviceGroup.Entry, ?, Map<Long, EnumeratedEndDeviceGroup.Entry>> toMap() {
        return Collectors.toMap(entry -> entry.getEndDevice().getId(), Function.identity());
    }

    private Condition getCondition(DeviceGroupInfo deviceGroupInfo) {
        Condition condition = Condition.TRUE;
        Object filterParam = deviceGroupInfo.filter;
        if (filterParam != null) {
            Map<String, Object> filter = deviceGroupInfo.filter;
            String mRID = (String) filter.get("mRID");
            if (!Checks.is(mRID).emptyOrOnlyWhiteSpace()) {
                condition = condition.and(where("mRID").likeIgnoreCase(mRID));
            }

            String serialNumber = (String) filter.get("serialNumber");
            if (!Checks.is(serialNumber).emptyOrOnlyWhiteSpace()) {
                condition = condition.and(where("serialNumber").likeIgnoreCase(serialNumber));
            }

            Object deviceTypesObject = filter.get("deviceTypes");
            if ((deviceTypesObject != null) && (deviceTypesObject instanceof List)) {
                List<Integer> deviceTypes = (List<Integer>) deviceTypesObject;
                if ((!deviceTypes.isEmpty())) {
                    Condition orCondition = Condition.FALSE;
                    for (int deviceTypeId : deviceTypes) {
                        Optional<DeviceType> deviceTypeOptional = deviceConfigurationService.findDeviceType(deviceTypeId);
                        if (deviceTypeOptional.isPresent()) {
                            DeviceType deviceType = deviceTypeOptional.get();
                            orCondition = orCondition.or(where("deviceConfiguration.deviceType.id").isEqualTo(deviceType.getId()));
                        }
                    }
                    condition = condition.and(orCondition);
                }
            }

            Object deviceConfigurationsObject = filter.get("deviceConfigurations");
            if ((deviceConfigurationsObject != null) && (deviceConfigurationsObject instanceof List)) {
                List<Integer> deviceConfigurations = (List) deviceConfigurationsObject;
                if ((deviceConfigurations != null) && (!deviceConfigurations.isEmpty())) {
                    Condition orCondition = Condition.FALSE;
                    for (int deviceConfigurationId : deviceConfigurations) {
                        Optional<DeviceConfiguration> deviceConfigurationOptional = deviceConfigurationService.findDeviceConfiguration(deviceConfigurationId);
                        if (deviceConfigurationOptional.isPresent()) {
                            DeviceConfiguration deviceConfiguration = deviceConfigurationOptional.get();
                            orCondition = orCondition.or(where("deviceConfiguration.id").isEqualTo(deviceConfiguration.getId()));
                        }
                    }
                    condition = condition.and(orCondition);
                }
            }

        }
        return condition;
    }

}