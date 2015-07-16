package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 14/07/15
 * Time: 09:53
 */
public class DestinationConnectionTaskIsEmpty extends LocalizedException {

    public DestinationConnectionTaskIsEmpty(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.DESTINATION_CONNECTION_TASK_IS_EMPTY);
    }

}
