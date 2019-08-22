/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface SAPCustomPropertySets {

    Optional<String> getSapDeviceId(Device device);

    Optional<String> getSapDeviceId(String deviceName);

    void addSapDeviceId(Device device, String sapDeviceId);

    Optional<Device> getDevice(String sapDeviceId);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<String, RangeSet<Instant>> getLrn(Channel channel, Range<Instant> range);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.common.device.data.Channel channel, Range<Instant> range);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.common.device.data.Register register, Range<Instant> range);

    void setLrn(Register register, String lrn, Instant startDateTime, Instant endDateTime);

    void setLrn(com.energyict.mdc.common.device.data.Channel channel, String lrn, Instant startDateTime, Instant endDateTime);

    boolean isAnyLrnPresent(long deviceId);

    Optional<Channel> getChannel(String lrn, Instant when);

    void setLocation(Device device, String locationId);

    void setPod(Device device, String podId);

    Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> getChannelInfos(String lrn, Range<Instant> interval);

    Optional<ChannelSpec> getChannelSpecForProfileId(ChannelSpec channelSpec, long deviceId, String profileId, Range<Instant> interval);

    Set<ReadingType> findReadingTypesForProfileId(String profileId);

    Map<Pair<String, String>, RangeSet<Instant>> getLrnAndProfileId(Channel channel, Range<Instant> range);
}