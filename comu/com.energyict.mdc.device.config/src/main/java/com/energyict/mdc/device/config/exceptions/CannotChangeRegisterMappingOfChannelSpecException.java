package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.masterdata.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link RegisterMapping}
 * of an existing {@link com.energyict.mdc.device.config.ChannelSpec}
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 13:30
 */
public class CannotChangeRegisterMappingOfChannelSpecException extends LocalizedException {

    public CannotChangeRegisterMappingOfChannelSpecException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CHANNEL_SPEC_CANNOT_CHANGE_REGISTER_MAPPING);
    }
}
