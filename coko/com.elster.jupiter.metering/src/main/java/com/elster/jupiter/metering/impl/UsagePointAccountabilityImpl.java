package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

import java.util.Date;

public class UsagePointAccountabilityImpl implements UsagePointAccountability {
	
	private Interval interval;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	private String userName;
	
	//Associations
	private Reference<UsagePoint> usagePoint = ValueReference.absent();
	private Reference<Party> party = ValueReference.absent();
	private Reference<PartyRole> role = ValueReference.absent();

    private final DataModel dataModel;
    private final PartyService partyService;
    private final Clock clock;

    @Inject
	UsagePointAccountabilityImpl(DataModel dataModel, PartyService partyService, Clock clock) {
        this.dataModel = dataModel;
        this.partyService = partyService;
        this.clock = clock;
    }
	
	UsagePointAccountabilityImpl init(UsagePoint usagePoint , Party party , PartyRole role , Date start) {
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

	public Date getCreateTime() {
		return createTime.toDate();
	}

	public Date getModTime() {
		return modTime.toDate();
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
}
