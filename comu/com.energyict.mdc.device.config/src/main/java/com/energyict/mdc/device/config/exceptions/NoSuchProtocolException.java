package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 31/03/14
 * Time: 13:14
 */
public class NoSuchProtocolException extends LocalizedFieldValidationException {
    public NoSuchProtocolException(Thesaurus thesaurus, String name, String fieldName) {
        super(thesaurus, MessageSeeds.PROTOCOL_INVALID_NAME, fieldName, name);
        this.set("name", name);
    }
}
