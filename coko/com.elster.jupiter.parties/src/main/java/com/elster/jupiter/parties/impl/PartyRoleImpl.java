package com.elster.jupiter.parties.impl;

import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.UtcInstant;

class PartyRoleImpl implements PartyRole {
	private String componentName;
	private String mRID;
	private String name;
	private String aliasName;
	private String description;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	
	@SuppressWarnings("unused")
	private PartyRoleImpl() {		
	}
	
	PartyRoleImpl(String componentName , String mRID , String name , String aliasName , String description) {
		this.componentName = componentName;
		this.mRID = mRID;
		this.name = name;
		this.aliasName = aliasName;
		this.description = description;
	}

	public String getComponentName() {
		return componentName;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getAliasName() {
		return aliasName;
	}

	@Override
	public String getDescription() {
		return description;
	}
}
