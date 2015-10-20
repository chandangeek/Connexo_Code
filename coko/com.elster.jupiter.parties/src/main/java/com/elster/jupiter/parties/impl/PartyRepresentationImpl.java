package com.elster.jupiter.parties.impl;

import java.time.Instant;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import java.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

final class PartyRepresentationImpl implements PartyRepresentation {

	private String delegate;
	private Interval interval;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	// associations
	private Reference<Party> party = ValueReference.absent();
    private User delegateUser;

    private final  UserService userService;
    private final Clock clock;
    
    @Inject
    PartyRepresentationImpl(Clock clock, UserService userService) {
    	this.clock = clock;
    	this.userService = userService;
    }
    
	PartyRepresentationImpl init(PartyImpl party, User delegate, Range<Instant> range) {
		this.party.set(party);
		this.delegateUser = Objects.requireNonNull(delegate);
		this.delegate = delegate.getName();
        this.interval = Interval.of(Objects.requireNonNull(range));
        return this;
	}
	
    @Override
    public User getDelegate() {
        if (delegateUser == null) {
            delegateUser = userService.findUser(delegate).orElse(null);
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

    @Override
    public void setRange(Range<Instant> range) {
        this.interval = Interval.of(range);
    }

	@Override
	public long getVersion() {
		return this.version;
	}

}
