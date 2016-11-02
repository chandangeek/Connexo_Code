package com.elster.jupiter.usagepoint.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface UsagePointMicroActionFactory {

    Optional<MicroAction> from(String microActionKey);
}
