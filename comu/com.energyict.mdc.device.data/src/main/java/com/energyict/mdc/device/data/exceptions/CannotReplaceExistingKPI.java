package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class CannotReplaceExistingKPI extends LocalizedException {

    public CannotReplaceExistingKPI(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.CAN_NOT_REPLACE_EXISTING_KPI);
    }

}