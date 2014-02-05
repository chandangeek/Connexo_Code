package com.energyict.mdc.device.config.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

/**
 * Copyrights EnergyICT
 * Date: 05/02/14
 * Time: 08:57
 */
public class CannotDeleteLoadProfileSpecLinkedChannelSpecsException extends LocalizedException{

    public CannotDeleteLoadProfileSpecLinkedChannelSpecsException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_DELETE_STILL_LINKED_CHANNEL_SPECS);
    }
}
