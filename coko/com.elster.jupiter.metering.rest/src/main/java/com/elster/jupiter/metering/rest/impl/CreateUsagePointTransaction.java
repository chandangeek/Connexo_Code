package com.elster.jupiter.metering.rest.impl;

import java.time.Clock;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.transaction.Transaction;

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
        if (serviceCategory.isPresent()) {
            return doPerform(serviceCategory.get());
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    private UsagePoint doPerform(ServiceCategory serviceCategory) {
        
        UsagePointBuilder upb = serviceCategory.newUsagePointBuilder();
        UsagePoint usagePoint = upb.withAliasName(info.aliasName)
        		.withDescription(info.description)
        		.withIsSdp(info.isSdp)
        		.withIsVirtual(info.isVirtual)
        		.withMRID(info.mRID)
        		.withName(info.name)
        		.withOutageRegion(info.outageRegion)
        		.withReadCycle(info.readCycle)
        		.withReadRoute(info.readRoute)
        		.withServicePriority(info.servicePriority)
        		.build();
        
        switch (serviceCategory.getKind()) {
        	case ELECTRICITY:
        		doPopulateElectricityDetails(usagePoint);
        		break;
		case GAS:
			break;
		case WATER:
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
		ElectricityDetailBuilder builder = usagePoint.newElectricityDetailBuilder(clock.instant());
		ElectricityDetail detail = builder.withAmiBillingReady(info.amiBillingReady)
			.withCheckBilling(info.checkBilling)
			.withConnectionState(info.connectionState)
			.withEstimatedLoad(info.estimatedLoad)
			.withGrounded(info.grounded)
			.withMinimalUsageExpected(info.minimalUsageExpected)
			.withNominalServiceVoltage(info.nominalServiceVoltage)
			.withPhaseCode(info.phaseCode)
			.withRatedCurrent(info.ratedCurrent)
			.withRatedPower(info.ratedPower)
			.withServiceDeliveryRemark(info.serviceDeliveryRemark)
			.build();
	}
}
