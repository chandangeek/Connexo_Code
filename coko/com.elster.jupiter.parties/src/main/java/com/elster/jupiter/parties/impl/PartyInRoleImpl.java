package com.elster.jupiter.parties.impl;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.Interval;

import java.util.Date;

public class PartyInRoleImpl implements PartyInRole {
	
	private long id;
	private long partyId;
	private String roleMRID;
	private Interval interval;
	
	private Party party;
	private PartyRole role;
	
	@SuppressWarnings("unused")
	private PartyInRoleImpl() {
	}
	
	PartyInRoleImpl(Party party , PartyRole role , Date start ) {
		this.party = party;
		this.partyId = party.getId();
		this.role = role;
		this.roleMRID = role.getMRID();
		this.interval = new Interval(start);
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
}
