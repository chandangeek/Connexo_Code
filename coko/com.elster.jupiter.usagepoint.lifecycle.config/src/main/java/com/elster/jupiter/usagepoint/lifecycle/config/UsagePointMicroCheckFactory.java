package com.elster.jupiter.usagepoint.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface UsagePointMicroCheckFactory {

    Optional<MicroCheck> from(String microActionKey);
}
