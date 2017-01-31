/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties;

import java.time.Instant;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.users.User;
import com.google.common.collect.Range;

@ProviderType
public interface PartyRepresentation  extends Effectivity {
    User getDelegate();
    Party getParty();
    boolean isCurrent();

    void setRange(Range<Instant> range);
    long getVersion();
}
