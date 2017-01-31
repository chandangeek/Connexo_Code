/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import com.elster.jupiter.cbo.IdentifiedObject;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;

@ProviderType
public interface PartyRole extends IdentifiedObject {
	String getComponentName();
    long getVersion();

    void setDescription(String description);

    void setName(String name);

    void setAliasName(String aliasName);

    List<Party> getParties();
    List<Party> getParties(Instant effectiveInstant);
}
