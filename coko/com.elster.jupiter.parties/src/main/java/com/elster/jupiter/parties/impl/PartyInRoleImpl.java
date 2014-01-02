package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import static com.google.common.base.Objects.*;

import java.util.Date;
import java.util.Objects;

import javax.inject.Inject;

public class PartyInRoleImpl implements PartyInRole {
	
	private long id;
	private Interval interval;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;
    
    private final Reference<Party> party = ValueReference.absent();
	private Reference<PartyRole> role = ValueReference.absent();
    
    private final DataModel dataModel;
    private final Clock clock;
	
    @Inject
    private PartyInRoleImpl(DataModel dataModel, Clock clock) {
    	this.dataModel = dataModel;
    	this.clock = clock;
    }
    
	PartyInRoleImpl init(Party party , PartyRole role , Interval interval) {
		this.party.set(Objects.requireNonNull(party));
		this.role.set(Objects.requireNonNull(role));
		this.interval = Objects.requireNonNull(interval);
		return this;
	}
	
	static PartyInRoleImpl from(DataModel dataModel, Party party, PartyRole role, Interval interval) {
		return dataModel.getInstance(PartyInRoleImpl.class).init(party, role, interval);
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

    void terminate(Date date) {
        if (!interval.isEffective(date)) {
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

    public UtcInstant getCreateTime() {
        return createTime;
    }

    public UtcInstant getModTime() {
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
