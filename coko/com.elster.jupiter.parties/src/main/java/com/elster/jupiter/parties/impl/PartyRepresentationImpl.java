package com.elster.jupiter.parties.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

final class PartyRepresentationImpl implements PartyRepresentation {

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
        this.delegateUser = delegate;
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

    @Override
    public User getDelegate() {
        if (delegateUser == null) {
            delegateUser = Bus.getUserService().findUser(delegate).orNull();
        }
		return delegateUser;
	}

	@Override
    public Party getParty() {
		if (party == null) {
			party = Bus.getOrmClient().getPartyFactory().getExisting(partyId);
		}			
		return party;
	}

    @Override
    public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean isCurrent() {
		return interval.isCurrent(Bus.getClock());
	}

    @Override
    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PartyRepresentationImpl that = (PartyRepresentationImpl) o;

        return delegate.equals(that.delegate) && party.equals(that.party);

    }

    @Override
    public int hashCode() {
        int result = delegate.hashCode();
        result = 31 * result + party.hashCode();
        return result;
    }
}
