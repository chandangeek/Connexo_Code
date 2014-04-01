package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 31/03/14
 * Time: 13:14
 */
public class NoSuchProtocolException extends LocalizedException {
    public NoSuchProtocolException(Thesaurus thesaurus, String name, String fieldName) {
        super(thesaurus, MessageSeeds.PROTOCOL_INVALID_NAME, name);
        this.set("name", name);
        this.setViolatingProperty(fieldName);
    }
}
