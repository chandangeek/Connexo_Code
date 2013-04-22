package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.time.UtcInstant;

public class ServiceCategoryImpl implements ServiceCategory {
	//persistent fields
	private ServiceKind kind;
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
	private ServiceCategoryImpl() {	
	}
	
	public ServiceCategoryImpl(ServiceKind kind) {
		this.kind = kind;		
	}
	
	public ServiceKind getKind() {	
		return kind;
	}
	
	public int getId() {
		return kind.ordinal() + 1;
	}

	@Override
	public String getName() {
		return kind.getDisplayName();
	}
	
	@Override
	public String getAliasName() {
		return aliasName;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	public void persist() {
		Bus.getOrmClient().getServiceCategoryFactory().persist(this);
	}
	
	public UsagePoint newUsagePoint(String mRid) {
		return new UsagePointImpl(mRid,this);
	}
}
