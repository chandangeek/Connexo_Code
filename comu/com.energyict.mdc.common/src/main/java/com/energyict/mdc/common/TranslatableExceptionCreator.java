package com.energyict.mdc.common;

import com.elster.jupiter.util.exception.MessageSeed;

public interface TranslatableExceptionCreator {
    public TranslatableApplicationException create(MessageSeed messageSeed);
}
