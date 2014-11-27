package com.elster.jupiter.parties.impl;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.time.Instant;
import java.util.Objects;

import javax.inject.Inject;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import java.time.Clock;
import com.elster.jupiter.util.time.Interval;

public class PartyInRoleImpl implements PartyInRole {
	
	private long id;
	private Interval interval;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;
    
    private Reference<Party> party = ValueReference.absent();
	private Reference<PartyRole> role = ValueReference.absent();
    
    private final Clock clock;
	
    @Inject
    PartyInRoleImpl(Clock clock) {
    	this.clock = clock;
    }
    
	PartyInRoleImpl init(Party party , PartyRole role , Interval interval) {
		this.party.set(Objects.requireNonNull(party));
		this.role.set(Objects.requireNonNull(role));
		this.interval = Objects.requireNonNull(interval);
		return this;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public Party getParty() {
		return party.get();
	}
		
	@Override
	public PartyRole getRole() {
		return role.get();
	}

	@Override
	public boolean isCurrent() {
		return interval.isCurrent(clock);
	}

	@Override
	public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean conflictsWith(PartyInRole other) {
        return getRole().equals(other.getRole()) && getParty().equals(other.getParty()) && interval.overlaps(other.getInterval());
    }

    void terminate(Instant date) {
        if (!isEffectiveAt(date)) {
            throw new IllegalArgumentException();
        }
        interval = interval.withEnd(date);
    }

    @Override
    public String toString() {
    	return toStringHelper(this).add("party", party).add("role", role).add("interval", interval).toString();
    }

    @Override
    public long getVersion() {
        return version;
    }

    public String getUserName() {
        return userName;
    }

    public Instant getCreateTime() {
        return createTime;
    }

    public Instant getModTime() {
        return modTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PartyInRoleImpl)) {
            return false;
        }

        PartyInRoleImpl that = (PartyInRoleImpl) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
