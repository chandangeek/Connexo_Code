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

    public CannotDeleteLoadProfileSpecLinkedChannelSpecsException(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }

}
