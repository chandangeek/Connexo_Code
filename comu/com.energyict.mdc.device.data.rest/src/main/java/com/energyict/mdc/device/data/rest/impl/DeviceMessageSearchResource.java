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
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
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
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

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
        List<DeviceMessage> deviceMessages = deviceMessageService.findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .from(queryParameters)
                .stream()
                .collect(toList());

        return PagedInfoList.fromPagedList("deviceMessages", deviceMessageInfoFactory.asFasterInfo(deviceMessages, uriInfo), queryParameters);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_DEVICE, Privileges.Constants.OPERATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_COMMUNICATION, Privileges.Constants.ADMINISTRATE_DEVICE_DATA})
    @Path("/{id}")
    public DeviceMessageInfo getDeviceMessage(@PathParam("id") long id, @Context UriInfo uriInfo) {
        DeviceMessage deviceMessage = deviceMessageService.findDeviceMessageById(id)
                .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_MESSAGE_WITH_ID, id));

        return deviceMessageInfoFactory.asFullInfo(deviceMessage, uriInfo);
    }

    private DeviceMessageQueryFilterImpl getDomainFilterFromExtjsQueryParams(@BeanParam JsonQueryFilter jsonQueryFilter) {
        DeviceMessageQueryFilterImpl deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl();
        List<Long> deviceGroupIds = jsonQueryFilter.getLongList("deviceGroups");
        if (!deviceGroupIds.isEmpty()) {
            List<EndDeviceGroup> endDeviceGroups = deviceGroupIds.stream()
                    .map(id -> meteringGroupService.findEndDeviceGroup(id)
                            .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_GROUP, id)))
                    .collect(toList());
            deviceMessageQueryFilter.setDeviceGroups(endDeviceGroups);
        }
        List<Integer> messageCategories = jsonQueryFilter.getIntegerList("messageCategories");
        if (!messageCategories.isEmpty()) {
            List<DeviceMessageCategory> deviceMessageCategories = messageCategories.stream()
                    .map(id -> deviceMessageSpecificationService.findCategoryById(id)
                            .orElseThrow(exceptionFactory.newExceptionSupplier(MessageSeeds.NO_SUCH_DEVICE_MESSAGE_CATEGORY, id)))
                    .collect(toList());
            deviceMessageQueryFilter.setMessageCategories(deviceMessageCategories);
        }
        List<String> deviceMessages = jsonQueryFilter.getStringList("deviceMessageIds");
        if (!deviceMessages.isEmpty()) {
            List<DeviceMessageId> deviceMessageIds = deviceMessages.stream()
                    .map(this::getDeviceMessageId)
                    .collect(toList());
            deviceMessageQueryFilter.setDeviceMessages(deviceMessageIds);
        }
        List<String> statuses = jsonQueryFilter.getStringList("statuses");
        if (!statuses.isEmpty()) {
            List<DeviceMessageStatus> deviceMessageStatuses = statuses.stream()
                    .map(this::getDeviceMessageStatus)
                    .collect(toList());
            deviceMessageQueryFilter.setDeviceMessagesStatuses(deviceMessageStatuses);
        }
        deviceMessageQueryFilter.setReleaseDateStart(jsonQueryFilter.getInstant("releaseDateStart"));
        deviceMessageQueryFilter.setReleaseDateEnd(jsonQueryFilter.getInstant("releaseDateEnd"));
        deviceMessageQueryFilter.setSentDateStart(jsonQueryFilter.getInstant("sentDateStart"));
        deviceMessageQueryFilter.setSentDateEnd(jsonQueryFilter.getInstant("sentDateEnd"));
        deviceMessageQueryFilter.setCreationDateStart(jsonQueryFilter.getInstant("creationDateStart"));
        deviceMessageQueryFilter.setCreationDateEnd(jsonQueryFilter.getInstant("creationDateEnd"));
        return deviceMessageQueryFilter;
    }

    private DeviceMessageId getDeviceMessageId(String name) {
        try {
            return DeviceMessageId.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw exceptionFactory.newException(MessageSeeds.NO_SUCH_DEVICE_COMMAND, name);
        }
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
        private Optional<Instant> releaseDateStart = Optional.empty();
        private Optional<Instant> releaseDateEnd = Optional.empty();
        private Optional<Instant> sentDateStart = Optional.empty();
        private Optional<Instant> sentDateEnd = Optional.empty();
        private Optional<Instant> creationDateStart = Optional.empty();
        private Optional<Instant> creationDateEnd = Optional.empty();

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

        @Override
        public Optional<Instant> getReleaseDateStart() {
            return this.releaseDateStart;
        }

        @Override
        public Optional<Instant> getReleaseDateEnd() {
            return this.releaseDateEnd;
        }

        @Override
        public Optional<Instant> getSentDateStart() {
            return this.sentDateStart;
        }

        @Override
        public Optional<Instant> getSentDateEnd() {
            return this.sentDateEnd;
        }

        @Override
        public Optional<Instant> getCreationDateStart() {
            return this.creationDateStart;
        }

        @Override
        public Optional<Instant> getCreationDateEnd() {
            return this.creationDateEnd;
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

        public void setReleaseDateStart(Instant releaseDateStart) {
            this.releaseDateStart = Optional.ofNullable(releaseDateStart);
        }

        public void setReleaseDateEnd(Instant releaseDateEnd) {
            this.releaseDateEnd = Optional.ofNullable(releaseDateEnd);
        }

        public void setSentDateStart(Instant sentDateStart) {
            this.sentDateStart = Optional.ofNullable(sentDateStart);
        }

        public void setSentDateEnd(Instant sentDateEnd) {
            this.sentDateEnd = Optional.ofNullable(sentDateEnd);
        }

        public void setCreationDateStart(Instant creationDateStart) {
            this.creationDateStart = Optional.ofNullable(creationDateStart);
        }

        public void setCreationDateEnd(Instant creationDateEnd) {
            this.creationDateEnd = Optional.ofNullable(creationDateEnd);
        }

    }
}
