package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;

public class NoLifeCycleActiveAt extends LocalizedException {

    public NoLifeCycleActiveAt(Instant time, Thesaurus thesaurus, MessageSeed messageSeed) {
        super(thesaurus, messageSeed, time.toString());
        set("time", time);
    }
}
