package com.elster.jupiter.ids.impl;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

public class MeasurementTimeIsNotValidExcecption extends LocalizedException {

    public MeasurementTimeIsNotValidExcecption(Thesaurus thesaurus) {
        super(thesaurus, MessageSeeds.MEASUREMENT_TIME_IS_INCORRECT);
    }

    public MeasurementTimeIsNotValidExcecption(Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed);
    }
}
