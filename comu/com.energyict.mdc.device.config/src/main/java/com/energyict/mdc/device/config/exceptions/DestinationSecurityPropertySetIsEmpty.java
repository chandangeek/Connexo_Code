package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.impl.MessageSeeds;

/**
 * Copyrights EnergyICT
 * Date: 14/07/15
 * Time: 10:40
 */
public class DestinationSecurityPropertySetIsEmpty extends LocalizedException {

    public DestinationSecurityPropertySetIsEmpty(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DESTINATION_SECURITY_PROPERTY_SET_IS_EMPTY);
    }
}
