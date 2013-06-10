package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


final class UpdateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;

    UpdateUsagePointTransaction(UsagePointInfo info) {
        this.info = info;
    }

    @Override
    public UsagePoint perform() {
        Optional<UsagePoint> usagePoint = Bus.getMeteringService().findUsagePoint(info.id);
        if (usagePoint.isPresent()) {
            return doPerform(usagePoint.get());
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private UsagePoint doPerform(UsagePoint usagePoint) {
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
