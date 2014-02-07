package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create an entity while there already exists an entity with that specific
 * {@link com.energyict.mdc.device.config.RegisterMapping}
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:57
 */
public class DuplicateRegisterMappingException extends LocalizedException {

    private DuplicateRegisterMappingException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

    /**
     * Creates a DuplicateRegisterMappingException  that models the
     * exceptional situation that occurs when an attempt is made to create
     * a {@link com.energyict.mdc.device.config.ChannelSpec} for a
     * {@link LoadProfileSpec} with a {@link RegisterMapping} while the
     * {@link LoadProfileSpec} already contains a {@link ChannelSpec}
     * with that {@link RegisterMapping}
     *
     * @param thesaurus       the Thesaurus
     * @param channelSpec     the ChannelSpec which already exists with the RegisterMapping
     * @param registerMapping the duplicate RegisterMapping
     * @param loadProfileSpec the LoadProfileSpec
     * @return the newly created DuplicateRegisterMappingException
     */
    public static DuplicateRegisterMappingException forChannelSpecInLoadProfileSpec(Thesaurus thesaurus, ChannelSpec channelSpec, RegisterMapping registerMapping, LoadProfileSpec loadProfileSpec) {
        DuplicateRegisterMappingException duplicateRegisterMappingException = new DuplicateRegisterMappingException(thesaurus, MessageSeeds.CHANNEL_SPEC_DUPLICATE_REGISTER_MAPPING_IN_LOAD_PROFILE_SPEC, loadProfileSpec, channelSpec, registerMapping);
        duplicateRegisterMappingException.set("loadProfileSpec", loadProfileSpec);
        duplicateRegisterMappingException.set("registerMapping", registerMapping);
        duplicateRegisterMappingException.set("channelSpec", channelSpec);
        return duplicateRegisterMappingException;
    }
}
