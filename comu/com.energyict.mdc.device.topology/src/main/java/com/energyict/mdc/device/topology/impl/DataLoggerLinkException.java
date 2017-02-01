/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.data.Device;

import java.time.Instant;

public class DataLoggerLinkException extends LocalizedException {

    static final String NO_MAPPING_FOR_ALL_SLAVE_CHANNELS = "DataLoggerLinkException.allSlaveChannelsShouldBeIncludedInTheMapping";
    static final String NO_FREE_DATA_LOGGER_CHANNEL = "DataLoggerLinkException.noFreeDataLoggerChannel";
    static final String NO_DATA_LOGGER_CHANNEL_FOR_READING_TYPE_X = "DataLoggerLinkException.noDataLoggerChannelForReadingTypeX";
    static final String NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X = "DataLoggerLinkException.noPhysicalSlaveChannelForReadingTypeX";
    static final String DEVICE_NOT_LINKED = "DataLoggerLinkException.DeviceNotLinked";

    private DataLoggerLinkException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static DataLoggerLinkException noPhysicalChannelForReadingType(Thesaurus thesaurus, ReadingType readingType) {
        return new DataLoggerLinkException(thesaurus, MessageSeeds.forKey(NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X), readingType.getMRID());
    }

    static DataLoggerLinkException invalidTerminationDate(Thesaurus thesaurus) {
        return new DataLoggerLinkException(thesaurus, MessageSeeds.DATA_LOGGER_LINK_INVALID_TERMINATION_DATE);
    }

    static DataLoggerLinkException slaveWasNotLinkedAt(Thesaurus thesaurus, Device slave, Instant when) {
        return new DataLoggerLinkException(thesaurus, MessageSeeds.DATA_LOGGER_SLAVE_NOT_LINKED_AT, slave.getName(), when);
    }

    static DataLoggerLinkException slaveWasPreviouslyLinkedAtSameTimeStamp(Thesaurus thesaurus, Device slave, Device datalogger, Instant linkingDate) {
        return new DataLoggerLinkException(thesaurus, MessageSeeds.DATA_LOGGER_UNIQUE_KEY_VIOLATION, slave.getName(), datalogger.getName(), linkingDate);
    }

    static DataLoggerLinkException slaveWasAlreadyLinkedToOtherDatalogger(Thesaurus thesaurus, Device slave, Device datalogger, Instant linkingDate) {
        return new DataLoggerLinkException(thesaurus, MessageSeeds.DATA_LOGGER_SLAVE_WAS_ALREADY_LINKED, slave.getName(), datalogger.getName(), linkingDate);
    }

}