package com.elster.jupiter.parties;

import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

public interface PartyRepresentation {
    User getDelegate();
    Party getParty();
    Interval getInterval();
    boolean isCurrent();

    void setInterval(Interval interval);
}
