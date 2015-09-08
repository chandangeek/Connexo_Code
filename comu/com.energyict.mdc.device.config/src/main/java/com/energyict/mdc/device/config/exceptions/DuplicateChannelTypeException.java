package com.energyict.mdc.device.config.exceptions;

import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.masterdata.MeasurementType;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import javax.validation.constraints.NotNull;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create an entity while there already exists an entity with that specific
 * {@link com.energyict.mdc.masterdata.MeasurementType}
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:57
 */
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
     * @return the newly created DuplicateChannelTypeException
     */
    public static DuplicateChannelTypeException forChannelSpecInLoadProfileSpec(@NotNull ChannelSpec channelSpec, @NotNull MeasurementType measurementType, LoadProfileSpec loadProfileSpec, Thesaurus thesaurus, MessageSeed messageSeed) {
        DuplicateChannelTypeException duplicateChannelTypeException = new DuplicateChannelTypeException(thesaurus, messageSeed, loadProfileSpec, channelSpec, measurementType.getReadingType().getAliasName());
        duplicateChannelTypeException.set("loadProfileSpec", loadProfileSpec);
        duplicateChannelTypeException.set("measurementType", measurementType);
        duplicateChannelTypeException.set("channelSpec", channelSpec);
        return duplicateChannelTypeException;
    }

}