package com.elster.jupiter.parties;

import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

public interface PartyRepresentation {

    User getDelegate();

    Party getParty();

    Interval getInterval();

    void setInterval(Interval interval);

    boolean isCurrent();
}
