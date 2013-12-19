package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Objects;

final class PartyRepresentationImpl implements PartyRepresentation {

	private String delegate;
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
	private final Reference<Party> party = ValueReference.absent();
    private User delegateUser;

	@SuppressWarnings("unused")
	private PartyRepresentationImpl() {	
	}
	
	PartyRepresentationImpl(Party party, User delegate, Interval interval) {
		this.party.set(party);
		this.delegateUser = Objects.requireNonNull(delegate);
		this.delegate = delegate.getName();
        this.interval = interval;
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
		return party.get();
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

}
