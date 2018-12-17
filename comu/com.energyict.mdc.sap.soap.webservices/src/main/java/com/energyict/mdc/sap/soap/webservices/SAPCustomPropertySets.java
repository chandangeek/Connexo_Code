/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices;

import com.elster.jupiter.metering.Channel;
import com.energyict.mdc.device.data.Device;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public interface SAPCustomPropertySets {

    Optional<BigDecimal> getSapDeviceId(Device device);

    Optional<BigDecimal> getSapDeviceId(String deviceName);

    Optional<Device> getDevice(BigDecimal sapDeviceId);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<BigDecimal, RangeSet<Instant>> getLrn(Channel channel, Range<Instant> range);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<BigDecimal, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Channel channel, Range<Instant> range);

    /**
     * It's not guaranteed what happens if the range is not fully contained in the channel lifetime.
     */
    Map<BigDecimal, RangeSet<Instant>> getLrn(com.energyict.mdc.device.data.Register register, Range<Instant> range);

    Optional<Channel> getChannel(BigDecimal lrn, Instant when);
}