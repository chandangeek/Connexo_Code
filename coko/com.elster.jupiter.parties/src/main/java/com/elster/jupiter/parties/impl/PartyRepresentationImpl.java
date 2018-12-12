/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Interval;

import com.google.common.collect.Range;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

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

    private final UserService userService;
    private final Clock clock;

    @Inject
    PartyRepresentationImpl(UserService userService, Clock clock) {
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