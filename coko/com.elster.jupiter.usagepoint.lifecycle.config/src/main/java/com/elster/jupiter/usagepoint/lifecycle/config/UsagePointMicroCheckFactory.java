package com.elster.jupiter.usagepoint.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;
import java.util.Set;

@ConsumerType
public interface UsagePointMicroCheckFactory {

    Optional<MicroCheck> from(String microActionKey);

    Set<MicroCheck> getAllChecks();
}
