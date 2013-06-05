package com.elster.jupiter.parties.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;

import java.util.Date;

public class PartyInRoleImpl implements PartyInRole {
	
	private long id;
	private long partyId;
	private String roleMRID;
	private Interval interval;
	
	private Party party;
	private PartyRole role;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

	@SuppressWarnings("unused")
	private PartyInRoleImpl() {
	}
	
	PartyInRoleImpl(Party party , PartyRole role , Interval interval) {
		this.party = party;
		this.partyId = party.getId();
		this.role = role;
		this.roleMRID = role.getMRID();
		this.interval = interval;
	}
	

	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public Party getParty() {
		if (party == null) {
			party = Bus.getOrmClient().getPartyFactory().getExisting(partyId);
		}
		return party;
	}
		
	@Override
	public PartyRole getRole() {
		if (role == null) {
			role = Bus.getOrmClient().getPartyRoleFactory().getExisting(roleMRID);
		}
		return role;
	}

	@Override
	public boolean isCurrent() {
		return interval.isCurrent(Bus.getClock());
	}

	@Override
	public Interval getInterval() {
        return interval;
    }

    @Override
    public boolean conflictsWith(PartyInRole other) {
        return role.equals(other.getRole()) && party.equals(other.getParty()) && interval.overlaps(other.getInterval());
    }

    void terminate(Date date) {
        if (!interval.contains(date)) {
            throw new IllegalArgumentException();
        }
        interval = interval.withEnd(date);
    }

    @Override
    public String toString() {
        return "PartyInRole{" +
                "party=" + party +
                ", role=" + role +
                ", interval=" + interval +
                '}';
    }

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
        return (int) (id ^ (id >>> 32));
    }
}
