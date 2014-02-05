package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to change the {@link com.energyict.mdc.device.config.LoadProfileType}
 * of an existing {@link com.energyict.mdc.device.config.LoadProfileSpec}
 * <p/>
 *
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 08:24
 */
public class CannotChangeLoadProfileTypeOfLoadProfileSpecException extends LocalizedException {

    public CannotChangeLoadProfileTypeOfLoadProfileSpecException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_CHANGE_LOAD_PROFILE_TYPE);
    }
}
