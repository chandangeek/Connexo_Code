package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;

public class UsagePointBuilderImpl implements UsagePointBuilder {
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private boolean isSdp;
	private boolean isVirtual;
	private String outageRegion;
	private String readCycle;
	private String readRoute;
	private String servicePriority;
	
	private ServiceCategory serviceCategory;
	private UsagePointImpl usagePoint;
	
	public UsagePointBuilderImpl(ServiceCategory sc, UsagePointImpl usagePointImpl) {
		this.serviceCategory=sc;
		this.usagePoint = usagePointImpl;
	}
	
	@Override
	public UsagePointBuilder withAliasName(String aliasName) {
		this.aliasName = aliasName;
		return this;
	}
	
	@Override
	public UsagePointBuilder withDescription(String description) {
		this.description = description;
		return this;
	}
	
	@Override
	public UsagePointBuilder withMRID(String mRID) {
		this.mRID = mRID;
		return this;
	}
	
	@Override
	public UsagePointBuilder withName(String name) {
		this.name = name;
		return this;
	}
	
	@Override
	public UsagePointBuilder withIsSdp(Boolean isSdp) {
		this.isSdp = isSdp;
		return this;
	}
	
	@Override
	public UsagePointBuilder withIsVirtual(Boolean isVirtual) {
		this.isVirtual = isVirtual;
		return this;
	}
	
	@Override
	public UsagePointBuilder withOutageRegion(String outageRegion) {
		this.outageRegion = outageRegion;
		return this;
	}
	
	@Override
	public UsagePointBuilder withReadCycle(String readCycle) {
		this.readCycle = readCycle;
		return this;
	}
	
	@Override
	public UsagePointBuilder withReadRoute(String readRoute) {
		this.readRoute = readRoute;
		return this;
	}
	
	@Override
	public UsagePointBuilder withServicePriority(String servicePriority) {
		this.servicePriority = servicePriority;
		return this;
	}
	
	@Override
	public UsagePoint build() {
		return usagePoint.init(this);
	}

	@Override
	public String getAliasName() {
		return aliasName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getmRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSdp() {
		return isSdp;
	}

	@Override
	public boolean isVirtual() {
		return isVirtual;
	}

	@Override
	public String getOutageRegion() {
		return outageRegion;
	}

	@Override
	public String getReadCycle() {
		return readCycle;
	}

	@Override
	public String getReadRoute() {
		return readRoute;
	}

	@Override
	public String getServicePriority() {
		return servicePriority;
	}
	
	@Override
	public ServiceCategory getServiceCategory() {
		return serviceCategory;
	}
	
}
