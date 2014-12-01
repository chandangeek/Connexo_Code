package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.rest.util.RestQuery;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.rest.ExceptionFactory;
import com.energyict.mdc.common.rest.PagedInfoList;
import com.energyict.mdc.common.rest.QueryParameters;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.security.Privileges;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.elster.jupiter.util.conditions.Where.where;

@Path("/devicegroups")
public class DeviceGroupResource {

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
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PagedInfoList getDeviceGroups(@BeanParam QueryParameters queryParameters, @QueryParam("type") String typeName) {

        com.elster.jupiter.rest.util.QueryParameters koreQueryParameters =
                com.elster.jupiter.rest.util.QueryParameters.wrap(queryParameters.getQueryParameters());
        Query<EndDeviceGroup> query;
        if (QueryEndDeviceGroup.class.getSimpleName().equalsIgnoreCase(typeName)) {
            query = meteringGroupsService.getQueryEndDeviceGroupQuery();
        } else {
            query = meteringGroupsService.getEndDeviceGroupQuery();
        }
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        List<EndDeviceGroup> allDeviceGroups = restQuery.select(koreQueryParameters, Order.ascending("upper(name)"));
        List<DeviceGroupInfo> deviceGroupInfos = DeviceGroupInfo.from(allDeviceGroups);
        return PagedInfoList.asJson("devicegroups", deviceGroupInfos, queryParameters);
    }

    private List<EndDeviceGroup> queryEndDeviceGroups(com.elster.jupiter.rest.util.QueryParameters queryParameters) {
        Query<EndDeviceGroup> query = meteringGroupsService.getEndDeviceGroupQuery();
        RestQuery<EndDeviceGroup> restQuery = restQueryService.wrap(query);
        return restQuery.select(queryParameters, Order.ascending("upper(name)"));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed(Privileges.ADMINISTRATE_DEVICE_GROUP)
    public Response createDeviceGroup(DeviceGroupInfo deviceGroupInfo) {
        Optional optional = meteringGroupsService.findEndDeviceGroupByName(deviceGroupInfo.name);
        if (optional.isPresent()) {
            throw exceptionFactory.newException(MessageSeeds.DEVICEGROUPNAME_ALREADY_EXISTS, deviceGroupInfo.name);
        }
        String name = deviceGroupInfo.name;
        boolean dynamic = deviceGroupInfo.dynamic;
        Condition condition = Condition.TRUE;
        Object filterParam = deviceGroupInfo.filter;
        if ((filterParam != null) && (filterParam instanceof LinkedHashMap)) {
            LinkedHashMap filter = (LinkedHashMap) deviceGroupInfo.filter;
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
                List<Integer> deviceTypes = (List) deviceTypesObject;
                if ((deviceTypes != null) && (!deviceTypes.isEmpty())) {
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

        EndDeviceGroup endDeviceGroup;
        if (dynamic) {
            endDeviceGroup = meteringGroupsService.createQueryEndDeviceGroup(condition);
            endDeviceGroup.setName(name);
            endDeviceGroup.setQueryProviderName("com.energyict.mdc.device.data.impl.DeviceEndDeviceQueryProvider");
        } else {
            endDeviceGroup = meteringGroupsService.createEnumeratedEndDeviceGroup(name);
            List<Integer> devices = (List<Integer>) deviceGroupInfo.devices;
            if (devices != null)  {
                for (int deviceId : devices) {
                    Optional<EndDevice> deviceOptional = meteringService.findEndDevice(deviceId);
                    if (deviceOptional.isPresent()) {
                        ((EnumeratedEndDeviceGroup) endDeviceGroup).add(deviceOptional.get(), Interval.sinceEpoch().toClosedRange());
                    }
                }
            // all devices option selected
            } else {
                Finder<Device> allDevicesFinder = deviceService.findAllDevices(condition);
                List<Device> allDevices = allDevicesFinder.find();
                for (Device device : allDevices) {
                    Optional<EndDevice> deviceOptional = meteringService.findEndDevice(device.getId());
                    if (deviceOptional.isPresent()) {
                        ((EnumeratedEndDeviceGroup) endDeviceGroup).add(deviceOptional.get(), Interval.sinceEpoch().toClosedRange());
                    }
                }
            }
        }
        endDeviceGroup.setLabel("MDC");
        endDeviceGroup.setMRID("MDC:" + endDeviceGroup.getName());
        endDeviceGroup.save();


        return Response.ok().build();
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
            value = value.replaceAll("\\*","%");
            return value;
        }
        if (value.contains("?")) {
            value = value.replaceAll("\\?","_");
            return value;
        }
        if (value.contains("%")) {
            return value;
        }
        return value;
    }



}
