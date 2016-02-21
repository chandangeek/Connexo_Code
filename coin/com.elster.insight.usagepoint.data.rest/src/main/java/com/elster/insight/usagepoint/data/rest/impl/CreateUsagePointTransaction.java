package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.time.Clock;
import java.util.Optional;

final class CreateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;

    private final MeteringService meteringService;

    private Clock clock;

    @Inject
    CreateUsagePointTransaction(UsagePointInfo info, MeteringService meteringService, Clock clock) {
        this.info = info;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @Override
    public UsagePoint perform() {
        Optional<ServiceCategory> serviceCategory = meteringService.getServiceCategory(info.serviceCategory);
        return serviceCategory
                .map(this::doPerform)
                .orElseThrow(() -> new WebApplicationException(Response.Status.NOT_FOUND));
    }

    private UsagePoint doPerform(ServiceCategory serviceCategory) {

        UsagePointBuilder upb = serviceCategory.newUsagePoint(info.mRID);
        UsagePoint usagePoint = upb
                .withIsSdp(info.isSdp)
                .withIsVirtual(info.isVirtual)
                .withName(info.name)
                .withOutageRegion(info.outageRegion)
                .withReadRoute(info.readRoute)
                .withServicePriority(info.servicePriority)
                .create();

        switch (serviceCategory.getKind()) {
            case ELECTRICITY:
                doPopulateElectricityDetails(usagePoint);
                break;
            case WATER:
                doPopulateWaterDetails(usagePoint);
                break;
            case GAS:
                doPopulateGasDetails(usagePoint);
                break;
            case HEAT:
            case INTERNET:
            case OTHER:
            case RATES:
            case REFUSE:
            case SEWERAGE:
            case TIME:
            case TVLICENSE:
                break;

        }

        return usagePoint;
    }

    private void doPopulateElectricityDetails(UsagePoint usagePoint) {
    }

    private void doPopulateGasDetails(UsagePoint usagePoint) {
        GasDetailBuilder builder = usagePoint.newGasDetailBuilder(clock.instant());
        GasDetail detail = builder
                .build();
    }

    private void doPopulateWaterDetails(UsagePoint usagePoint) {
        WaterDetailBuilder builder = usagePoint.newWaterDetailBuilder(clock.instant());
        WaterDetail detail = builder
                .build();
    }
}
