package com.elster.jupiter.parties;

import com.elster.jupiter.orm.associations.Effectivity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface PartyInRole extends Effectivity {
	long getId();
	Party getParty();
	PartyRole getRole();
	boolean isCurrent();

    /**
     * @param partyInRole
     * @return true if the argument defines the same role for the same party, and its interval overlaps this instance's interval.
     */
	boolean conflictsWith(PartyInRole partyInRole);

    long getVersion();
}
