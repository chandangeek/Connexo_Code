package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

/**
 * Copyrights EnergyICT
 * Date: 12/03/14
 * Time: 10:52
 */
public class PartialConnectionTaskDoesNotExist extends LocalizedException {

    public PartialConnectionTaskDoesNotExist(Thesaurus thesaurus, String name) {
        super(thesaurus, MessageSeeds.PARTIAL_CONNECTION_TASK_NAME_DOES_NOT_EXIST, name);
    }

    public PartialConnectionTaskDoesNotExist(Thesaurus thesaurus, int id) {
        super(thesaurus, MessageSeeds.PARTIAL_CONNECTION_TASK_ID_DOES_NOT_EXIST, id);
    }
}
