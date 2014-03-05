package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.device.config.ChannelSpec;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a new entity within this bundle without
 * specifying an {@link com.energyict.mdc.common.interval.Phenomenon}.
 * <p/> *
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:07
 */
public class PhenomenonIsRequiredException extends LocalizedException {

    private PhenomenonIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}
