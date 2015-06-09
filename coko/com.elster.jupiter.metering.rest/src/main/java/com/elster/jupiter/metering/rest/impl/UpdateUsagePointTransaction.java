package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.rest.UsagePointInfo;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import java.time.Clock;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.Principal;


final class UpdateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;
    private final Principal principal;
    private final MeteringService meteringService;
    private final Clock clock;

    @Inject
    UpdateUsagePointTransaction(UsagePointInfo info, Principal principal, MeteringService meteringService, Clock clock) {
        this.info = info;
        this.principal = principal;
        this.meteringService = meteringService;
        this.clock = clock;
    }

    @Override
    public UsagePoint perform() {
        Optional<UsagePoint> usagePoint = meteringService.findUsagePoint(info.id);
        if (usagePoint.isPresent()) {
            return doPerform(usagePoint.get());
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    private UsagePoint doPerform(UsagePoint usagePoint) {
        if (isAllowedToEdit(usagePoint)) {
            if (usagePoint.getVersion() == info.version) {
                usagePoint.setMRID(info.mRID);
                usagePoint.setAliasName(info.aliasName);
                usagePoint.setDescription(info.description);
                usagePoint.setName(info.name);
                usagePoint.setSdp(info.isSdp);
                usagePoint.setVirtual(info.isVirtual);
                usagePoint.setOutageRegion(info.outageRegion);
                usagePoint.setReadCycle(info.readCycle);
                usagePoint.setReadRoute(info.readRoute);
                usagePoint.setServicePriority(info.servicePriority);
                UsagePointDetail detail = usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant());
                detail.setAmiBillingReady(info.amiBillingReady);
                detail.setCheckBilling(info.checkBilling);
                detail.setConnectionState(info.connectionState);
                detail.setMinimalUsageExpected(info.minimalUsageExpected);
                if (detail instanceof ElectricityDetail) {
                	ElectricityDetail eDetail = (ElectricityDetail) detail;
                	eDetail.setEstimatedLoad(info.estimatedLoad);
	                eDetail.setGrounded(info.grounded);
	                eDetail.setNominalServiceVoltage(info.nominalServiceVoltage);
	                eDetail.setPhaseCode(info.phaseCode);
	                eDetail.setRatedCurrent(info.ratedCurrent);
	                eDetail.setRatedPower(info.ratedPower);
	                eDetail.setServiceDeliveryRemark(info.serviceDeliveryRemark);
	                eDetail.setPhaseCode(info.phaseCode);
	                eDetail.setRatedPower(info.ratedPower);
                }
                usagePoint.addDetail(detail);
                usagePoint.save();
            } else {
                throw new WebApplicationException(Response.Status.CONFLICT);
            }
            return usagePoint;
        }
        throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    private boolean isAllowedToEdit(UsagePoint usagePoint) {
        return isOwn(usagePoint) || hasEditAllPrivilege();
    }

    private boolean hasEditAllPrivilege() {
        return principal instanceof User && ((User) principal).hasPrivilege(Privileges.ADMIN_ANY);
    }

    private boolean isOwn(UsagePoint usagePoint) {
        for (UsagePointAccountability accountability : usagePoint.getAccountabilities()) {
            for (PartyRepresentation representation : accountability.getParty().getCurrentDelegates()) {
                if (representation.getDelegate().equals(principal)) {
                    return true;
                }
            }
        }
        return false;
    }
}
