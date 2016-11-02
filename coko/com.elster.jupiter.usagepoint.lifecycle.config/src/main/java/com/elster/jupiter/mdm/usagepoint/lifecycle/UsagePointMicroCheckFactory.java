package com.elster.jupiter.mdm.usagepoint.lifecycle;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface UsagePointMicroCheckFactory {

    Optional<MicroCheck> from(String microActionKey);
}
