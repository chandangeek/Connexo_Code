package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.RegisterMapping;

/**
 * Models the exceptional situation that occurs when an attempt
 * is made to update the {@link com.energyict.mdc.common.ObisCode}
 * of a {@link LoadProfileType} that is in use.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (11:22)
 */
public class CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException extends LocalizedException {

    public CannotUpdateObisCodeWhenLoadProfileTypeIsInUseException(Thesaurus thesaurus, LoadProfileType loadProfileType) {
        super(thesaurus, MessageSeeds.LOAD_PROFILE_TYPE_OBIS_CODE_CANNOT_BE_UPDATED, loadProfileType.getName());
        this.set("loadProfileType", loadProfileType);
    }

}