package com.elster.jupiter.parties;

import java.time.Instant;

import com.elster.jupiter.orm.associations.Effectivity;
import com.elster.jupiter.users.User;
import com.google.common.collect.Range;

public interface PartyRepresentation  extends Effectivity {
    User getDelegate();
    Party getParty();
    boolean isCurrent();

    void setRange(Range<Instant> range);
}
