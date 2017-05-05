/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl.multielement;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.impl.MessageSeeds;

import java.time.Instant;

public class MultiElementDeviceLinkException extends LocalizedException {

    static final String NO_MAPPING_FOR_ALL_SLAVE_CHANNELS = "MultiElementDeviceLinkException.allSlaveChannelsShouldBeIncludedInTheMapping";
    static final String NO_FREE_DATA_LOGGER_CHANNEL = "MultiElementDeviceLinkException.noFreeDataLoggerChannel";
    static final String NO_DATA_LOGGER_CHANNEL_FOR_READING_TYPE_X = "MultiElementDeviceLinkException.noDataLoggerChannelForReadingTypeX";
    static final String NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X = "MultiElementDeviceLinkException.noPhysicalSlaveChannelForReadingTypeX";
    static final String DEVICE_NOT_LINKED = "MultiElementDeviceLinkException.DeviceNotLinked";

    private MultiElementDeviceLinkException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static MultiElementDeviceLinkException noPhysicalChannelForReadingType(Thesaurus thesaurus, ReadingType readingType) {
        return new MultiElementDeviceLinkException(thesaurus, MessageSeeds.forKey(NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X), readingType.getMRID());
    }

    static MultiElementDeviceLinkException invalidTerminationDate(Thesaurus thesaurus) {
        return new MultiElementDeviceLinkException(thesaurus, MessageSeeds.DATA_LOGGER_LINK_INVALID_TERMINATION_DATE);
    }

    static MultiElementDeviceLinkException slaveWasNotLinkedAt(Thesaurus thesaurus, Device slave, Instant when) {
        return new MultiElementDeviceLinkException(thesaurus, MessageSeeds.DATA_LOGGER_SLAVE_NOT_LINKED_AT, slave.getName(), when);
    }

    static MultiElementDeviceLinkException slaveWasPreviouslyLinkedAtSameTimeStamp(Thesaurus thesaurus, Device slave, Device datalogger, Instant linkingDate) {
        return new MultiElementDeviceLinkException(thesaurus, MessageSeeds.DATA_LOGGER_UNIQUE_KEY_VIOLATION, slave.getName(), datalogger.getName(), linkingDate);
    }

    static MultiElementDeviceLinkException slaveWasAlreadyLinkedToOtherDatalogger(Thesaurus thesaurus, Device slave, Device datalogger, Instant linkingDate) {
        return new MultiElementDeviceLinkException(thesaurus, MessageSeeds.DATA_LOGGER_SLAVE_WAS_ALREADY_LINKED, slave.getName(), datalogger.getName(), linkingDate);
    }

}