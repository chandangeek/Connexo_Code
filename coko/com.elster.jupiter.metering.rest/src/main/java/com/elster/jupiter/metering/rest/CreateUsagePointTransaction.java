package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.transaction.Transaction;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

final class CreateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;

    CreateUsagePointTransaction(UsagePointInfo info) {
        this.info = info;
    }

    @Override
    public UsagePoint perform() {
        Optional<ServiceCategory> serviceCategory = Bus.getMeteringService().getServiceCategory(info.serviceCategory);
        if (serviceCategory.isPresent()) {
            return doPerform(serviceCategory);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private UsagePoint doPerform(Optional<ServiceCategory> serviceCategory) {
        UsagePoint usagePoint = serviceCategory.get().newUsagePoint(info.mRID);
        usagePoint.setPhaseCode(info.phaseCode);
        usagePoint.save();
        return usagePoint;
    }
}
