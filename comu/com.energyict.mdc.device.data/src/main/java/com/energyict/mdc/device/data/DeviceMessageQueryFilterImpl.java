/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DeviceMessageQueryFilterImpl implements DeviceMessageQueryFilter {

    private Optional<Device> device = Optional.empty();
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
    public Optional<Device> getDevice() {
        return this.device;
    }

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

    public void setDevice(Device device) {
        this.device = Optional.ofNullable(device);
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
