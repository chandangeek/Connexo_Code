package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class FirmwareVersionIsInUseException extends LocalizedException {

    public FirmwareVersionIsInUseException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.VERSION_IN_USE);
    }
}
