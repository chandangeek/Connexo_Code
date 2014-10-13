package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

import javax.inject.Inject;

import java.time.Instant;
import java.util.Date;

public class UsagePointAccountabilityImpl implements UsagePointAccountability {
	
	private Interval interval;
	private long version;
	private Instant createTime;
	private Instant modTime;
	private String userName;
	
	//Associations
	private Reference<UsagePoint> usagePoint = ValueReference.absent();
	private Reference<Party> party = ValueReference.absent();
	private Reference<PartyRole> role = ValueReference.absent();

    private final Clock clock;

    @Inject
	UsagePointAccountabilityImpl(Clock clock) {
        this.clock = clock;
    }
	
	UsagePointAccountabilityImpl init(UsagePoint usagePoint , Party party , PartyRole role , Instant start) {
		this.usagePoint.set(usagePoint);
		this.party.set(party);
		this.role.set(role);
		this.interval = Interval.startAt(start);
        return this;
	}

	public long getUsagePointId() {
		return usagePoint.get().getId();
	}

	public long getPartyId() {
		return party.get().getId();
	}

	public String getRoleMRID() {
		return role.get().getMRID();
	}

	public Interval getInterval() {
		return interval;
	}

	public long getVersion() {
		return version;
	}

	public Instant getCreateTime() {
		return createTime;
	}

	public Instant getModTime() {
		return modTime;
	}

	public String getUserName() {
		return userName;
	}

	public UsagePoint getUsagePoint() {
		return usagePoint.get();
	}

	public Party getParty() {
        return party.get();
	}

	public PartyRole getRole() {
		return role.get();
	}

	@Override
	public Date getStart() {
		return interval.getStart();
	}

	@Override
	public Date getEnd() {
		return interval.getEnd();
	}

	@Override
	public boolean isCurrent() {
		return interval.isCurrent(clock);
	}
	
	@Override
	public boolean isEffective(Date when) {
		return interval.isEffective(when);
	}
}
