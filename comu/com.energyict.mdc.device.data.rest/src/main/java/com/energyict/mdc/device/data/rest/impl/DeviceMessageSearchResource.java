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
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.google.common.collect.ImmutableList;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
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
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public DeviceMessageSearchResource(DeviceMessageService deviceMessageService, DeviceMessageInfoFactory deviceMessageInfoFactory, MeteringGroupsService meteringGroupService, ExceptionFactory exceptionFactory, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageService = deviceMessageService;
        this.deviceMessageInfoFactory = deviceMessageInfoFactory;
        this.meteringGroupService = meteringGroupService;
        this.exceptionFactory = exceptionFactory;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    public PagedInfoList getDeviceMessages(@BeanParam JsonQueryParameters queryParameters, @BeanParam JsonQueryFilter jsonQueryFilter, @Context UriInfo uriInfo) {
        DeviceMessageQueryFilterImpl deviceMessageQueryFilter = getDomainFilterFromExtjsQueryParams(jsonQueryFilter);
        List<DeviceMessageInfo> deviceMessageInfos = deviceMessageService.findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .from(queryParameters)
                .stream()
                .map(dm -> deviceMessageInfoFactory.asInfo(dm, uriInfo))
                .collect(Collectors.toList());

        return PagedInfoList.fromPagedList("deviceMessages", deviceMessageInfos, queryParameters);
    }

    private DeviceMessageQueryFilterImpl getDomainFilterFromExtjsQueryParams(@BeanParam JsonQueryFilter jsonQueryFilter) {
        DeviceMessageQueryFilterImpl deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl();
        List<Long> deviceGroupIds = jsonQueryFilter.getLongList("deviceGroups");
        if (!deviceGroupIds.isEmpty()) {
            List<EndDeviceGroup> endDeviceGroups = deviceGroupIds.stream()
                    .map(id -> meteringGroupService.findEndDeviceGroup(id)
                            .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_GROUP, id)))
                    .collect(Collectors.toList());
            deviceMessageQueryFilter.setDeviceGroups(endDeviceGroups);
        }
        List<Integer> messageCategories = jsonQueryFilter.getIntegerList("messageCategories");
        if (!messageCategories.isEmpty()) {
            List<DeviceMessageCategory> deviceMessageCategories = messageCategories.stream()
                    .map(id -> deviceMessageSpecificationService.findCategoryById(id)
                            .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE_CATEGORY, id)))
                    .collect(Collectors.toList());
            deviceMessageQueryFilter.setMessageCategories(deviceMessageCategories);
        }
        List<Integer> deviceMessages = jsonQueryFilter.getIntegerList("deviceMessageIds");
        if (!deviceMessages.isEmpty()) {
            List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                    .map(DeviceMessageId::from)
                    .collect(Collectors.toList());
            deviceMessageQueryFilter.setDeviceMessages(deviceMessageIds);
        }
        List<String> statuses = jsonQueryFilter.getStringList("statuses");
        if (!statuses.isEmpty()) {
            List<DeviceMessageStatus> deviceMessageStatuses = statuses.stream()
                    .map(this::getDeviceMessageStatus)
                    .collect(Collectors.toList());
            deviceMessageQueryFilter.setDeviceMessagesStatuses(deviceMessageStatuses);
        }
        return deviceMessageQueryFilter;
    }

    private DeviceMessageStatus getDeviceMessageStatus(String name) {
        try {
            return DeviceMessageStatus.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_MESSAGE_STATUS, name);
        }
    }

    private class DeviceMessageQueryFilterImpl implements DeviceMessageQueryFilter {

        private List<EndDeviceGroup> endDeviceGroups = Collections.emptyList();
        private List<DeviceMessageCategory> deviceMessageCategories = Collections.emptyList();
        private List<DeviceMessageId> deviceMessageIds = Collections.emptyList();
        private List<DeviceMessageStatus> statuses = Collections.emptyList();

        @Override
        public Collection<EndDeviceGroup> getDeviceGroups() {
            return this.endDeviceGroups;
        }

        @Override
        public Collection<DeviceMessageCategory> getMessageCategories() {
            return this.deviceMessageCategories;
        }

        @Override
        public Collection<DeviceMessageId> getDeviceMessages() {
            return this.deviceMessageIds;
        }

        @Override
        public Collection<DeviceMessageStatus> getStatuses() {
            return this.statuses;
        }

        public void setDeviceGroups(List<EndDeviceGroup> endDeviceGroups) {
            this.endDeviceGroups = ImmutableList.copyOf(endDeviceGroups);
        }

        public void setMessageCategories(List<DeviceMessageCategory> deviceMessageCategories) {
            this.deviceMessageCategories = ImmutableList.copyOf(deviceMessageCategories);
        }

        public void setDeviceMessages(List<DeviceMessageId> deviceMessageIds) {
            this.deviceMessageIds = ImmutableList.copyOf(deviceMessageIds);
        }

        public void setDeviceMessagesStatuses(List<DeviceMessageStatus> deviceMessageStatuses) {
            this.statuses = ImmutableList.copyOf(deviceMessageStatuses);
        }
    }
}
