package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.energyict.mdc.device.data.DeviceMessageQueryFilter;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.security.Privileges;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resource to search for devices with a filter
 */
@Path("/devicemessages")
public class DeviceMessageSearchResource {

    private final DeviceMessageService deviceMessageService;
    private final DeviceMessageInfoFactory deviceMessageInfoFactory;
    private final MeteringGroupsService meteringGroupService;
    private final ExceptionFactory exceptionFactory;

    @Inject
    public DeviceMessageSearchResource(DeviceMessageService deviceMessageService, DeviceMessageInfoFactory deviceMessageInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory) {
        this.deviceMessageService = deviceMessageService;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getDeviceMessages(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter, @Context UriInfo uriInfo) {
        DeviceMessageQueryFilterImpl deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl();
        List<Integer> deviceGroupIds = jsonQueryFilter.getIntegerList("deviceGroups");
        if (!deviceGroupIds.isEmpty()) {
            deviceMessageQueryFilter.setDeviceGroups(deviceGroupIds.stream().map(id->meteringGroupService.findEndDeviceGroup(id).orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_GROUP))).collect(Collectors.toList()));
        }
        List<DeviceMessageInfo> deviceMessageInfos = deviceMessageService.findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .from(queryParameters)
                .stream()
                .map(dm -> deviceMessageInfoFactory.asInfo(dm, uriInfo))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("deviceMessages", deviceMessageInfos, queryParameters);
    }

    private class DeviceMessageQueryFilterImpl implements DeviceMessageQueryFilter {

        private List<EndDeviceGroup> endDeviceGroups;

        @Override
        public Collection<EndDeviceGroup> getDeviceGroups() {
            return Collections.unmodifiableList(this.endDeviceGroups);
        }

        public void setDeviceGroups(List<EndDeviceGroup> endDeviceGroups) {

            this.endDeviceGroups = endDeviceGroups;
        }
    }
}
