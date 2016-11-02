package com.elster.jupiter.mdm.usagepoint.lifecycle;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;

@ConsumerType
public interface UsagePointMicroActionFactory {

    Optional<MicroAction> from(String microActionKey);
}
