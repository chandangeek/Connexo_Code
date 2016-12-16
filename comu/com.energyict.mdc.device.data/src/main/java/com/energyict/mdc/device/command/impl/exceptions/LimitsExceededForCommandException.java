package com.energyict.mdc.device.command.impl.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.command.impl.MessageSeeds;

public class LimitsExceededForCommandException extends LocalizedException {
    public LimitsExceededForCommandException(Thesaurus thesaurus, MessageSeeds messageSeeds) {
        super(thesaurus, messageSeeds);
    }
}
