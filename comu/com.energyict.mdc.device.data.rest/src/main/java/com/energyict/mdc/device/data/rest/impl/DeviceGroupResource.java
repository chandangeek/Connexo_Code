package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.rest.util.ListPager;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;
import java.util.LinkedHashMap;
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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.elster.jupiter.util.streams.Functions.asStream;

@Path("/devicegroups")
public class DeviceGroupResource {

    private static final Logger LOGGER = Logger.getLogger(DeviceGroupResource.class.getName());

    private final MeteringGroupsService meteringGroupsService;
    private final RestQueryService restQueryService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceGroupResource(MeteringGroupsService meteringGroupsService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, RestQueryService restQueryService, DeviceService deviceService, ExceptionFactory exceptionFactory) {
        this.meteringGroupsService = meteringGroupsService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.restQueryService = restQueryService;
        this.deviceService = deviceService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Path("/{id}/")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.VIEW_DEVICE_GROUP_DETAIL})
    public DeviceGroupInfo getDeviceGroup(@PathParam("id") long id, @Context SecurityContext securityContext) {
        return DeviceGroupInfo.from(fetchDeviceGroup(id, securityContext), deviceConfigurationService, deviceService);
    }

    private EndDeviceGroup fetchDeviceGroup(long id, @Context SecurityContext securityContext) {
        return meteringGroupsService.findEndDeviceGroup(id).orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    @GET
    @Path("/{id}/devices")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.VIEW_DEVICE_GROUP_DETAIL})
    public PagedInfoList getDevices(@BeanParam QueryParameters queryParameters, @PathParam("id") long deviceGroupId, @Context SecurityContext securityContext) {
        EndDeviceGroup endDeviceGroup = fetchDeviceGroup(deviceGroupId, securityContext);
        List<? extends EndDevice> allEndDevices = endDeviceGroup.getMembers(Instant.now());
        List<? extends EndDevice> endDevices =
                ListPager.of(allEndDevices).paged(queryParameters.getStart(), queryParameters.getLimit()).find();

        List<Device> devices = new ArrayList<Device>();
        for (EndDevice endDevice : endDevices) {
            Device device = deviceService.findDeviceById(Long.parseLong(endDevice.getAmrId()));
            if (device != null) {
                devices.add(device);
            }
        }
        //List<Device> subList = devices.subList(0, Math.min(queryParameters.getLimit() + 1, endDevices.size() + 1));
        List<DeviceInfo> deviceInfos = DeviceInfo.from(devices);
        return PagedInfoList.asJson("devices", deviceInfos, queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    // not protected by privileges yet because a combobox containing al the groups needs to be shown when creating an export task
    public PagedInfoList getDeviceGroups(@BeanParam QueryParameters queryParameters, @QueryParam("type") String typeName, @Context UriInfo uriInfo) {

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
        List<DeviceGroupInfo> deviceGroupInfos = DeviceGroupInfo.from(allDeviceGroups, deviceConfigurationService, deviceService);
        return PagedInfoList.asJson("devicegroups", deviceGroupInfos, queryParameters);
    }

    private List<EndDeviceGroup> queryEndDeviceGroups(com.elster.jupiter.rest.util.QueryParameters queryParameters) {
        Query<EndDeviceGroup> query = meteringGroupsService.getEndDeviceGroupQuery();
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({Privileges.ADMINISTRATE_DEVICE_GROUP, Privileges.ADMINISTRATE_DEVICE_ENUMERATED_GROUP, Privileges.VIEW_DEVICE_GROUP_DETAIL})
    @Path("/{id}")
    public Response editDeviceGroup(DeviceGroupInfo deviceGroupInfo, @PathParam("id") long id) {
        EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(id)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

        endDeviceGroup.setName(deviceGroupInfo.name);

        if (deviceGroupInfo.dynamic) {
            ((QueryEndDeviceGroup) endDeviceGroup).setCondition(getCondition(deviceGroupInfo));
        } else {
            syncListWithInfo((EnumeratedEndDeviceGroup) endDeviceGroup, deviceGroupInfo);
        }
        endDeviceGroup.save();
        return Response.status(Response.Status.CREATED).entity(DeviceGroupInfo.from(endDeviceGroup, deviceConfigurationService, deviceService)).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_GROUP)
    @Path("/{id}")
    public Response removeDeviceGroup(DeviceGroupInfo deviceGroupInfo, @PathParam("id") long id) {
        try {
            EndDeviceGroup endDeviceGroup = meteringGroupsService.findEndDeviceGroup(id)
                    .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));

            endDeviceGroup.delete();
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new WebApplicationException(e.getLocalizedMessage());
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_GROUP)
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


        return Response.ok().build();
    }

    private void syncListWithInfo(EnumeratedEndDeviceGroup enumeratedEndDeviceGroup, DeviceGroupInfo deviceGroupInfo) {
        Stream<? extends Number> deviceIds = Optional.ofNullable((List<Integer>) deviceGroupInfo.devices).map(list -> list.stream()).orElse(null);
        if (deviceIds == null) {
            deviceIds = deviceService.findAllDevices(getCondition(deviceGroupInfo)).find().stream()
                    .map(HasId::getId);
        }
        List<EndDevice> endDevices = deviceIds.map(number -> meteringService.findEndDevice(number.longValue()))
                .flatMap(asStream())
                .collect(Collectors.toList());

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
        if ((filterParam != null) && (filterParam instanceof LinkedHashMap)) {
            LinkedHashMap<String, Object> filter = (LinkedHashMap<String, Object>) deviceGroupInfo.filter;
            String mRID = (String) filter.get("mRID");
            if ((mRID != null) && (!"".equals(mRID))) {
                mRID = replaceRegularExpression(mRID);
                condition = !isRegularExpression(mRID)
                        ? condition.and(where("mRID").isEqualTo(mRID))
                        : condition.and(where("mRID").like(mRID));
            }

            String serialNumber = (String) filter.get("serialNumber");
            if ((serialNumber != null) && (!"".equals(serialNumber))) {
                serialNumber = replaceRegularExpression(serialNumber);
                condition = !isRegularExpression(serialNumber)
                        ? condition.and(where("serialNumber").isEqualTo(serialNumber))
                        : condition.and(where("serialNumber").like(serialNumber));
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
                            orCondition = orCondition.or(where("deviceConfiguration.deviceType.name").isEqualTo(deviceType.getName()));
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
                            orCondition = orCondition.or(where("deviceConfiguration.name").isEqualTo(deviceConfiguration.getName()));
                        }
                    }
                    condition = condition.and(orCondition);
                }
            }

        }
        return condition;
    }

    private boolean isRegularExpression(String value) {
        if (value.contains("*")) {
            return true;
        }
        if (value.contains("?")) {
            return true;
        }
        if (value.contains("%")) {
            return true;
        }
        return false;
    }

    private String replaceRegularExpression(String value) {
        if (value.contains("*")) {
            value = value.replaceAll("\\*", "%");
            return value;
        }
        if (value.contains("?")) {
            value = value.replaceAll("\\?", "_");
            return value;
        }
        if (value.contains("%")) {
            return value;
        }
        return value;
    }


}
