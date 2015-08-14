package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;

import java.time.Instant;

public class NoMeterActivationAt extends LocalizedException {
    public NoMeterActivationAt(Thesaurus thesaurus, Instant time) {
        super(thesaurus, MessageSeeds.NO_METER_ACTIVATION_AT, time.toString());
        set("time", time);
    }
}
