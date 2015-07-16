package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 14/07/15
 * Time: 10:39
 */
public class OriginSecurityPropertySetIsEmpty extends LocalizedException {

    public OriginSecurityPropertySetIsEmpty(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.ORIGIN_SECURITY_PROPERTY_SET_IS_EMPTY);
    }
}
