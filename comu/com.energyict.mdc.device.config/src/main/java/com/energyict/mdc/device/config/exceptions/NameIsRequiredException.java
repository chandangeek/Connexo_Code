package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without specifying a name for it.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-29 (16:00)
 */
public class NameIsRequiredException extends LocalizedException {

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterGroup} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException registerGroupNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.REGISTER_GROUP_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.RegisterMapping} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException registerMappingNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.REGISTER_MAPPING_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LoadProfileType} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException loadProfileTypeNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.LogBookType} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException logBookTypeNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.LOG_BOOK_TYPE_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.DeviceType} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException deviceTypeNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.DEVICE_TYPE_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.common.interval.Phenomenon} without a name.
     *
     * @param thesaurus The Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException phenomenonNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.PHENOMENON_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create a
     * {@link com.energyict.mdc.device.config.ChannelSpec} without a name.
     *
     * @param thesaurus the Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException channelSpecNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.CHANNEL_SPEC_NAME_IS_REQUIRED);
    }

    /**
     * Creates a new NameIsRequiredException that models the exceptional
     * situation that occurs when an attempt is made to create a
     * {@link com.energyict.mdc.device.config.DeviceConfiguration} without a name.
     *
     * @param thesaurus the Thesaurus
     * @return The NameIsRequiredException
     */
    public static NameIsRequiredException deviceConfigurationNameIsRequired(Thesaurus thesaurus) {
        return new NameIsRequiredException(thesaurus, MessageSeeds.DEVICE_CONFIGURATION_NAME_IS_REQUIRED);
    }

    private NameIsRequiredException(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }
}