package com.elster.jupiter.parties;

import java.util.Date;

public interface PartyInRole {
	long getId();
	Party getParty();
	PartyRole getRole();
	boolean isCurrent();
	Date getStart();
	Date getEnd();
	
}
