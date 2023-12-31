/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

public class MeterAlreadyLinkedToUsagePoint extends LocalizedException {

    public MeterAlreadyLinkedToUsagePoint(Thesaurus thesaurus, MeterActivation meterActivation) {
        super(thesaurus, PrivateMessageSeeds.METER_ALREADY_LINKED_TO_USAGEPOINT,
                meterActivation.getMeter().map(Meter::getName).orElse(""),
                meterActivation.getUsagePoint().map(UsagePoint::getName).orElse(""),
                meterActivation.getMeterRole().map(MeterRole::getDisplayName).orElse(""));
    }

}