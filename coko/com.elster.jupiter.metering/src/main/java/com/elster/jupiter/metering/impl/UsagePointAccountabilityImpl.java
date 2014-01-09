package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.util.Date;

public class UsagePointAccountabilityImpl implements UsagePointAccountability {
	
	private long usagePointId;
	private long partyId;
	private String roleMRID;
	private Interval interval;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	private String userName;
	
	//Associations
	private UsagePoint usagePoint;
	private Party party;
	private PartyRole role;

    private final DataModel dataModel;
    private final PartyService partyService;
    private final Clock clock;

    @SuppressWarnings("unused")
    @Inject
	UsagePointAccountabilityImpl(DataModel dataModel, PartyService partyService, Clock clock) {
        this.dataModel = dataModel;
        this.partyService = partyService;
        this.clock = clock;
    }
	
	UsagePointAccountabilityImpl init(UsagePoint usagePoint , Party party , PartyRole role , Date start) {
		this.usagePoint = usagePoint;
		this.usagePointId = usagePoint.getId();
		this.party = party;
		this.partyId = party.getId();
		this.role = role;
		this.roleMRID = role.getMRID();
		this.interval = Interval.startAt(start);
        return this;
	}

    static UsagePointAccountabilityImpl from(DataModel dataModel, UsagePoint usagePoint , Party party , PartyRole role , Date start) {
        return dataModel.getInstance(UsagePointAccountabilityImpl.class).init(usagePoint, party, role, start);
    }

	public long getUsagePointId() {
		return usagePointId;
	}

	public long getPartyId() {
		return partyId;
	}

	public String getRoleMRID() {
		return roleMRID;
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
        if (usagePoint == null) {
            usagePoint = dataModel.mapper(UsagePoint.class).getOptional(usagePointId).get();
        }
		return usagePoint;
	}

	public Party getParty() {
        if (party == null) {
            party = partyService.findParty(partyId).get();
        }
        return party;
	}

	public PartyRole getRole() {
        if (role == null) {
            role = partyService.findPartyRoleByMRID(roleMRID).get();
        }
		return role;
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
