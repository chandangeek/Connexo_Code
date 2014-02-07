package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link com.energyict.mdc.device.config.LoadProfileSpec}
 * of an existing {@link com.energyict.mdc.device.config.ChannelSpec}
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 13:37
 */
public class CannotChangeLoadProfileSpecOfChannelSpec extends LocalizedException{

    public CannotChangeLoadProfileSpecOfChannelSpec(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CHANNEL_SPEC_CANNOT_CHANGE_LOAD_PROFILE_SPEC);
    }
}
