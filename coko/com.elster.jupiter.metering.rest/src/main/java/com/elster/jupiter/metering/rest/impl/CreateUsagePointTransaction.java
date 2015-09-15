package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Optional;

final class CreateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;

    private final MeteringService meteringService;

    @Inject
    CreateUsagePointTransaction(UsagePointInfo info, MeteringService meteringService) {
        this.info = info;
        this.meteringService = meteringService;
    }

    @Override
    public UsagePoint perform() {
        Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(info.serviceCategory);
        if (serviceCategory.isPresent()) {
            return doPerform(serviceCategory);
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private UsagePoint doPerform(Optional<ServiceCategory> serviceCategory) {
        return serviceCategory.get().newUsagePoint(info.mRID).create();
    }
}
