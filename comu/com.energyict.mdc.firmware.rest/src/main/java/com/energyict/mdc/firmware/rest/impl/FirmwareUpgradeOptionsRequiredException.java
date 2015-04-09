package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class FirmwareUpgradeOptionsRequiredException extends LocalizedException {

    public FirmwareUpgradeOptionsRequiredException(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.UPGRADE_OPTIONS_REQUIRED);
    }
}
