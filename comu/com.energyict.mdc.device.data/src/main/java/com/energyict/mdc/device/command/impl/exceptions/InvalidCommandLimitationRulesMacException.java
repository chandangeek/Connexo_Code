package com.energyict.mdc.device.command.impl.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.command.impl.MessageSeeds;

public class InvalidCommandLimitationRulesMacException extends LocalizedException{
    public InvalidCommandLimitationRulesMacException(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }
}
