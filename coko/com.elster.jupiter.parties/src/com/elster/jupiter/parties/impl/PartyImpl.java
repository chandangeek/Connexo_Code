package com.elster.jupiter.parties.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.cbo.TelephoneNumber;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class PartyImpl implements Party {
	
	private long id;
	private String mRID;
	private String name;
	private String aliasName;
	private String description;
	private ElectronicAddress electronicAddress;
	private TelephoneNumber phone1;
	private TelephoneNumber phone2;
	
	// associations
	List<PartyInRole> partyInRoles;
	
	PartyImpl() {		
	}

    @Override
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

    /**
     * @return Master Resource Identifier
     */
    @Override
	public String getMRID() {
		return mRID;
	}

	public void setMRID(String mRID) {
		this.mRID = mRID;
	}

    @Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

    @Override
	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

    @Override
	public ElectronicAddress getElectronicAddress() {
		return electronicAddress == null ? null : electronicAddress.copy();
	}

	public void setElectronicAddress(ElectronicAddress electronicAddress) {
		this.electronicAddress = electronicAddress == null ? null : electronicAddress.copy();
	}

	public TelephoneNumber getPhone1() {
		return phone1 == null ? null : phone1.copy();
	}

	public void setPhone1(TelephoneNumber phone1) {
		this.phone1 = phone1 == null ? null : phone1.copy();
	}

	public TelephoneNumber getPhone2() {
		return phone2 == null ? null : phone2.copy();
	}

	public void setPhone2(TelephoneNumber phone2) {
		this.phone2 = phone2 == null ? null : phone2.copy();
	}

	public List<PartyInRole> getPartyInRoles() {
		if (partyInRoles == null) {
			partyInRoles = Bus.getOrmClient().getPartyInRoleFactory().find("party",this);
		}
		return partyInRoles;
	}
	
	// ORM inheritance map
	static final Map<String,Class<? extends Party>> implementers = new HashMap<>();
	{
		implementers.put("ORGANIZATION", OrganizationImpl.class);
		implementers.put("PERSON", PersonImpl.class);
	}

}
