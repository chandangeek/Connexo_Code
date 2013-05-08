package com.elster.jupiter.parties.impl;

import java.util.Date;

import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.time.Interval;

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
			party = Bus.getOrmClient().getPartyFactory().get(partyId);
		}
		return party;
	}
		
	@Override
	public PartyRole getRole() {
		if (role == null) {
			role = Bus.getOrmClient().getPartyRoleFactory().get(roleMRID);
		}
		return role;
	}

	@Override
	public boolean isCurrent() {
		return interval.isCurrent();
	}

	

	@Override
	public Date getStart() {
		return interval.getStart();
	}

	@Override
	public Date getEnd() {
		return interval.getEnd();
	}
}
