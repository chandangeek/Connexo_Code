package com.elster.jupiter.metering;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class MeterAlreadyLinkedToUsagePoint extends LocalizedException {

    protected MeterAlreadyLinkedToUsagePoint(Thesaurus thesaurus, Meter meter, UsagePoint prior) {
        super(thesaurus, MessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT, meter.getName(), prior.getName());
    }

}
