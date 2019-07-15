/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

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
    Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Channel channel, Range<Instant> range);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<String, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Register register, Range<Instant> range);

    void setLrn(Register register, String lrn, Instant startDateTime, Instant endDateTime);

    boolean isAnyLrn(long deviceId);

    Optional<Channel> getChannel(String lrn, Instant when);

    void setLocation(Device device, String locationId);

    void setPod(Device device, String podId);
}