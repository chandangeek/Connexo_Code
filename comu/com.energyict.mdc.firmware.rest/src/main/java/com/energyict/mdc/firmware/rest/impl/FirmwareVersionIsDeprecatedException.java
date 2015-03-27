package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class FirmwareVersionIsDeprecatedException  extends LocalizedException {

    public FirmwareVersionIsDeprecatedException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.VERSION_IS_DEPRECATED);
    }
}
