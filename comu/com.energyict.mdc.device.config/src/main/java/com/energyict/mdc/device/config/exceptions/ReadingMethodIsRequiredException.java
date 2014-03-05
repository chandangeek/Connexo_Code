package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Models the exceptional situation that occurs when an attempt is made
 * to create a {@link com.energyict.mdc.device.config.ChannelSpec}
 * without a {@link com.energyict.mdc.protocol.api.device.ReadingMethod}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 07/02/14
 * Time: 10:27
 */
public class ReadingMethodIsRequiredException extends LocalizedException {

    private ReadingMethodIsRequiredException(Thesaurus thesaurus, MessageSeed messageSeed, Object... args) {
        super(thesaurus, messageSeed, args);
    }

}
