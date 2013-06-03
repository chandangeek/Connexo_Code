package com.elster.jupiter.parties;

import com.elster.jupiter.util.time.Interval;

public interface PartyInRole {
	long getId();
	Party getParty();
	PartyRole getRole();
	boolean isCurrent();
	Interval getInterval();
	
}
