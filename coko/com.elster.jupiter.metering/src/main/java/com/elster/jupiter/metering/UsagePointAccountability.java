package com.elster.jupiter.metering;

import java.util.Date;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;

public interface UsagePointAccountability {
	public UsagePoint getUsagePoint();
	public Party getParty();
	public PartyRole getRole();
	public Date getStart();
	public Date getEnd();
	public boolean isCurrent();
}
