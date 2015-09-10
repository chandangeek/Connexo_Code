package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.impl.MessageSeeds;

/**
 * Copyrights EnergyICT
 * Date: 14/07/15
 * Time: 09:51
 */
public class OriginConnectionTaskIsEmpty extends LocalizedException {

    public OriginConnectionTaskIsEmpty(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.ORIGIN_CONNECTION_TASK_IS_EMPTY);
    }
}
