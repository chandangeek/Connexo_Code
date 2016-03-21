package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class MetrologyPurposeDeletionFail extends LocalizedException {

    public MetrologyPurposeDeletionFail(Thesaurus thesaurus, String metrologyPurposeName) {
        super(thesaurus, MessageSeeds.CAN_NOT_DELETE_METROLOGY_PURPOSE_IN_USE, metrologyPurposeName);
    }
}
