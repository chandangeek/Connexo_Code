package com.energyict.mdc.device.data.exceptions;

import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import java.time.Instant;

public class NoLifeCycleActiveAt extends LocalizedException {

    public NoLifeCycleActiveAt(Thesaurus thesaurus, MessageSeed messageSeed, Instant shipmentDate, Instant maxPast, Instant maxFuture) {
        super(thesaurus, messageSeed, shipmentDate, maxPast, maxFuture);
        set("shipmentDate", shipmentDate);
        set("maxPast", maxPast);
        set("maxFuture", maxFuture);
    }
}
