package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.RegisterSpec;

/**
 * Models the exceptional situation that occurs when an
 * attempt is made to create a {@link com.energyict.mdc.device.config.RegisterSpec}
 * with a linked {@link com.energyict.mdc.device.config.ChannelSpec} that is already used
 * <p/>
 * Copyrights EnergyICT
 * Date: 03/02/14
 * Time: 12:04
 */
public class DuplicatePrimeRegisterSpecException extends LocalizedException {

    public DuplicatePrimeRegisterSpecException(Thesaurus thesaurus, ChannelSpec channelSpec, RegisterSpec currentPrimeRegisterSpec) {
        super(thesaurus, MessageSeeds.REGISTER_SPEC_PRIME_CHANNEL_SPEC_ALREADY_EXISTS, channelSpec.getId(), currentPrimeRegisterSpec.getId());
        set("channelSpec", channelSpec);
        set("currentPrimeRegisterSpec", currentPrimeRegisterSpec);
    }

}
