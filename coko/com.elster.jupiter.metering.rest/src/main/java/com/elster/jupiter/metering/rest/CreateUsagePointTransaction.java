package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.transaction.Transaction;

final class CreateUsagePointTransaction implements Transaction<UsagePoint> {
	private final UsagePointInfo info;

    CreateUsagePointTransaction(UsagePointInfo info) {
		this.info = info;
	}

    @Override
	public UsagePoint perform() {
        UsagePoint usagePoint = Bus.getMeteringService().getServiceCategory(info.serviceCategory).newUsagePoint(info.mRID);
		usagePoint.setPhaseCode(info.phaseCode);
		usagePoint.save();
        return usagePoint;
	}
}
