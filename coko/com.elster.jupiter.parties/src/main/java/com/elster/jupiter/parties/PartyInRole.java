package com.elster.jupiter.parties;

import com.elster.jupiter.util.time.Interval;

public interface PartyInRole {
	long getId();
	Party getParty();
	PartyRole getRole();
	boolean isCurrent();
	Interval getInterval();

    /**
     * @param partyInRole
     * @return true if the argument defines the same role for the same party, and its interval overlaps this instance's interval.
     */
	boolean conflictsWith(PartyInRole partyInRole);

    long getVersion();
}
