package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.UsagePoint;


public class UpdateUsagePointTransaction implements Runnable {
	final private UsagePointInfo info;
	private UsagePoint usagePoint;
	
	UpdateUsagePointTransaction(UsagePointInfo info) {
		this.info = info;
	}

	UsagePoint execute() {
		Bus.getServiceLocator().getTransactionService().execute(this);
		return usagePoint;
	}
	@Override
	public void run() {
		usagePoint = Bus.getServiceLocator().getMeteringService().findUsagePoint(info.id);
		if (usagePoint.getVersion() == info.version) {
			usagePoint.setMRID(info.mRID);
			usagePoint.setPhaseCode(info.phaseCode);
			usagePoint.setRatedPower(info.ratedPower);
			usagePoint.save();
		} else {
			System.out.println("Failed");
			throw new RuntimeException("Optimistic lock failed");
		}
	}
}
