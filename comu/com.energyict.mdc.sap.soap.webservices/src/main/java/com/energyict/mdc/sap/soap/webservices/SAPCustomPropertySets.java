/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;

import aQute.bnd.annotation.ProviderType;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface SAPCustomPropertySets {

    Thesaurus getThesaurus();

    Optional<String> getSapDeviceId(Device device);

    Optional<String> getSapDeviceId(EndDevice endDevice);

    /**
     * @deprecated Please use {@link #getSapDeviceId(Device)} or {@link #getSapDeviceId(EndDevice)}.
     */
    @Deprecated
    Optional<String> getSapDeviceId(String deviceName);

    void setSapDeviceId(Device device, String sapDeviceId);

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

    Set<Pair<Long, ChannelSpec>> getChannelInfosAfterDate(String lrn, String profileId, Instant date);

    /**
     * This method returns map containing as key <Device id, Channel spec> info and as value list of ranges where LRN is defined and overlaps interval.
     * Every item in the list of ranges is pair, where first part is range intersected with interval and the second part is full LRN range in CAS.
     */
    Map<Pair<Long, ChannelSpec>, List<Pair<Range<Instant>, Range<Instant>>>> getChannelInfos(String lrn, Range<Instant> interval);

    Optional<ChannelSpec> getChannelSpecForProfileId(ChannelSpec channelSpec, long deviceId, String profileId, Range<Instant> interval);

    Set<ReadingType> findReadingTypesForProfileId(String profileId);

    Map<String, RangeSet<Instant>> getProfileId(Channel channel, Range<Instant> range);

    void truncateCpsInterval(Device device, String lrn, Instant endDate);

    Optional<Interval> getLastProfileIdIntervalForChannelOnDevice(long deviceId, String readingTypeMrid);

    boolean doesRegisterHaveSapCPS(Register register);

    boolean doesChannelHaveSapCPS(com.energyict.mdc.common.device.data.Channel channel);

    boolean isAnyProfileIdPresent(List<ChannelsContainer> channelsContainers, ReadingType readingType, Range<Instant> range);

    /**
     * This method returns first date of the Profile id set for the reading container
     * In case of the first range doesn't have lower bound, then 1970-01-01T00:00:00Z date is returned.
     */
    Optional<Instant> getFirstDateWithSetProfileId(ReadingContainer readingContainer, ReadingType readingType);

    /**
     * This method returns start of the first LRN after the latest transition to operational stage
     * or the date of this transition, if no such LRN is present.
     */
    Optional<Instant> getStartDate(Device device);
}
