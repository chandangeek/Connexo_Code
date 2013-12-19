package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Objects;

import javax.inject.Inject;

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

    // transient 
    private final UserService userService;
    private final Clock clock;
    
	@Inject
	private PartyRepresentationImpl(UserService userService,Clock clock) {
		this.userService = userService;
		this.clock = clock;
	}
	
	PartyRepresentationImpl init(Party party, User delegate, Interval interval) {
		this.party.set(party);
		this.delegateUser = Objects.requireNonNull(delegate);
		this.delegate = delegate.getName();
        this.interval = Objects.requireNonNull(interval);
        return this;
	}

    @Override
    public User getDelegate() {
        if (delegateUser == null) {
            delegateUser = userService.findUser(delegate).orNull();
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
		return interval.isCurrent(clock);
	}

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

}
