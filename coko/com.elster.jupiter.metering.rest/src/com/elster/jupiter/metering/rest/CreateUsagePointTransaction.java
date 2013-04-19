package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.UsagePoint;

public class CreateUsagePointTransaction implements Runnable {
	final private UsagePointInfo info;
	private UsagePoint usagePoint;
	
	CreateUsagePointTransaction(UsagePointInfo info) {
		this.info = info;
	}

	UsagePoint execute() {
		Bus.getServiceLocator().getTransactionService().execute(this);
		return usagePoint;
	}
	@Override
	public void run() {
		usagePoint = Bus.getServiceLocator().getMeteringService().getServiceCategory(info.serviceCategory).newUsagePoint(info.mRID);		
		usagePoint.setPhaseCode(info.phaseCode);
		usagePoint.save();
	}
}
