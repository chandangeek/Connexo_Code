/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import aQute.bnd.annotation.ConsumerType;

import java.util.Optional;
import java.util.Set;

@ConsumerType
public interface UsagePointMicroActionFactory {

    Optional<MicroAction> from(String microActionKey);

    Set<MicroAction> getAllActions();
}
