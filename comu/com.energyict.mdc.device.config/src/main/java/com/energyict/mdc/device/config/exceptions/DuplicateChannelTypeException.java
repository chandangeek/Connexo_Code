/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.MeasurementType;

import javax.validation.constraints.NotNull;

public class DuplicateChannelTypeException extends LocalizedException {

    private DuplicateChannelTypeException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a DuplicateChannelTypeException  that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} for a
     * {@link LoadProfileSpec} with a {@link com.energyict.mdc.masterdata.MeasurementType} while the
     * {@link LoadProfileSpec} already contains a {@link ChannelSpec}
     * with that {@link com.energyict.mdc.masterdata.MeasurementType}
     *
     * @param channelSpec     the ChannelSpec which already exists with the ChannelType
     * @param measurementType the duplicate ChannelType
     * @param loadProfileSpec the LoadProfileSpec
     * @param thesaurus       the Thesaurus
     * @param messageSeed The MessageSeed
     * @param duplicateReadingType
     * @return the newly created DuplicateChannelTypeException
     */
    public static DuplicateChannelTypeException duplicateChannelSpecOnDeviceConfiguration(@NotNull ChannelSpec channelSpec, @NotNull MeasurementType measurementType, LoadProfileSpec loadProfileSpec, Thesaurus thesaurus, MessageSeed messageSeed, ReadingType duplicateReadingType) {
        DuplicateChannelTypeException duplicateChannelTypeException = new DuplicateChannelTypeException(thesaurus, messageSeed, loadProfileSpec.getDeviceConfiguration().getName(), channelSpec, duplicateReadingType.getFullAliasName());
        duplicateChannelTypeException.set("loadProfileSpec", loadProfileSpec);
        duplicateChannelTypeException.set("measurementType", measurementType);
        duplicateChannelTypeException.set("channelSpec", channelSpec);
        duplicateChannelTypeException.set("duplicateReadingType", duplicateReadingType);
        return duplicateChannelTypeException;
    }

}