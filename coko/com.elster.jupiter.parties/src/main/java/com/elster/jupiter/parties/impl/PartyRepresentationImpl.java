package com.elster.jupiter.parties.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.users.User;
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
    private User delegateUser;

	@SuppressWarnings("unused")
	private PartyRepresentationImpl() {	
	}
	
	PartyRepresentationImpl(Party party, User delegate, Interval interval) {
        validateParty(party);
        validateUser(delegate);
        this.partyId = party.getId();
		this.party = party;
		this.delegate = delegate.getName();
		this.interval = interval;
	}

    private void validateUser(User delegate) {
        if (delegate == null) {
            throw new IllegalArgumentException("User as delegate, can not be null.");
        }
    }

    private void validateParty(Party party) {
        if (party == null) {
            throw new IllegalArgumentException("Party cannot be null");
        }
    }

    public User getDelegate() {
        if (delegateUser == null) {
            delegateUser = Bus.getUserService().findUser(delegate).orNull();
        }
		return delegateUser;
	}

	public Party getParty() {
		if (party == null) {
			party = Bus.getOrmClient().getPartyFactory().getExisting(partyId);
		}			
		return party;
	}

    public Interval getInterval() {
        return interval;
    }

    public boolean isCurrent() {
		return interval.isCurrent(Bus.getClock());
	}

    void setInterval(Interval interval) {
        this.interval = interval;
    }
}
