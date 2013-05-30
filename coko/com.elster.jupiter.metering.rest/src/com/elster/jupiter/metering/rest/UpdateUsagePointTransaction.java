package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.transaction.Transaction;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


final class UpdateUsagePointTransaction implements Transaction<UsagePoint> {
	final private UsagePointInfo info;

    UpdateUsagePointTransaction(UsagePointInfo info) {
		this.info = info;
	}

    @Override
	public UsagePoint perform() {
        UsagePoint usagePoint = Bus.getServiceLocator().getMeteringService().findUsagePoint(info.id);
		if (usagePoint.getVersion() == info.version) {
			usagePoint.setMRID(info.mRID);
			usagePoint.setPhaseCode(info.phaseCode);
			usagePoint.setRatedPower(info.ratedPower);
			usagePoint.save();
		} else {
			throw new WebApplicationException(Response.Status.CONFLICT);			
		}
        return usagePoint;
	}
}
