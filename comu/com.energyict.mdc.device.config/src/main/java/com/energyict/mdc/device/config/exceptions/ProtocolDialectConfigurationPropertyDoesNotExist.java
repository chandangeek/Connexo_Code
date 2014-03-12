package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 12/03/14
 * Time: 10:35
 */
public class ProtocolDialectConfigurationPropertyDoesNotExist extends LocalizedException {

    public ProtocolDialectConfigurationPropertyDoesNotExist(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.PROTOCOL_DIALECT_NAME_DOES_NOT_EXIST, name);
    }

    public ProtocolDialectConfigurationPropertyDoesNotExist(Thesaurus thesaurus, int id) {
        super(thesaurus, MessageSeeds.PROTOCOL_DIALECT_ID_DOES_NOT_EXIST, id);
    }
}
