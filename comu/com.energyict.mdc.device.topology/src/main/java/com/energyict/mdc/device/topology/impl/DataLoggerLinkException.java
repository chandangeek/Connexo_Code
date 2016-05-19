package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Copyrights EnergyICT
 * Date: 28/04/2016
 * Time: 15:29
 */
public class DataLoggerLinkException extends LocalizedException {

    final static String NO_MAPPING_FOR_ALL_SLAVE_CHANNELS = "DataLoggerLinkException.allSlaveChannelsShouldBeIncludedInTheMapping";
    final static String NO_FREE_DATA_LOGGER_CHANNEL = "DataLoggerLinkException.noFreeDataLoggerChannel";
    final static String NO_DATA_LOGGER_CHANNEL_FOR_READING_TYPE_X = "DataLoggerLinkException.noDataLoggerChannelForReadingTypeX";
    final static String NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X = "DataLoggerLinkException.noPhysicalSlaveChannelForReadingTypeX";
    final static String DEVICE_NOT_LINKED = "DataLoggerLinkException.DeviceNotLinked";

    private DataLoggerLinkException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    public static DataLoggerLinkException noPhysicalChannelForReadingType(Thesaurus thesaurus, ReadingType readingType){
        return new DataLoggerLinkException(thesaurus, MessageSeeds.forKey(NO_PHYSICAL_CHANNEL_FOR_READING_TYPE_X), readingType.getMRID());
    }
}
