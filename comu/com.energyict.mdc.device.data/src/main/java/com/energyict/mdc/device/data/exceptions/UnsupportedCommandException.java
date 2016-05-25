package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.data.impl.MessageSeeds;

public final class UnsupportedCommandException extends LocalizedException {

    public UnsupportedCommandException(Thesaurus thesaurus, String command, String mRID) {
        super(thesaurus, MessageSeeds.UNSPPORTED_COMMAND, command, mRID);
    }
}
