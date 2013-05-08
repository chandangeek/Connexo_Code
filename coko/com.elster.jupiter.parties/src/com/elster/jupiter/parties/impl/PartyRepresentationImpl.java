package com.elster.jupiter.parties.impl;

import java.util.Date;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

class PartyRepresentationImpl  {
	private String delegate;
	private long partyId;
	private Interval interval;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private Party party;
	
	@SuppressWarnings("unused")
	private PartyRepresentationImpl() {	
	}
	
	PartyRepresentationImpl(String delegate , Party party , Date at) {
		this.partyId = party.getId();
		this.party = party;
		this.delegate = delegate;
		this.interval = new Interval(at);
	}

	public String getDelegate() {	
		return delegate;
	}

	public Party getParty() {
		if (party == null) {
			party  = Bus.getOrmClient().getPartyFactory().get(partyId);
		}			
		return party;
	}
	
	public Date getStart() {
		return interval.getStart();
	}

	public Date getEnd() {
		return interval.getEnd();
	}

	public boolean isCurrent() {
		return interval.isCurrent();
	}

}
