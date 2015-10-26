package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.rest.UsagePointInfo;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.rest.util.ConcurrentModificationExceptionFactory;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.time.Clock;

final class UpdateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;
    private final Principal principal;
    private final MeteringService meteringService;
    private final Clock clock;
    private final ConcurrentModificationExceptionFactory conflictFactory;

    @Inject
    UpdateUsagePointTransaction(UsagePointInfo info, Principal principal, MeteringService meteringService, Clock clock, ConcurrentModificationExceptionFactory conflictFactory) {
        this.info = info;
        this.principal = principal;
        this.meteringService = meteringService;
        this.clock = clock;
        this.conflictFactory = conflictFactory;
    }

    @Override
    public UsagePoint perform() {
        UsagePoint usagePoint = meteringService.findAndLockUsagePointByIdAndVersion(info.id, info.version)
                .orElseThrow(conflictFactory.contextDependentConflictOn(info.name)
                        .withActualVersion(() -> meteringService.findUsagePoint(info.id).map(UsagePoint::getVersion).orElse(null))
                        .supplier());
        return doPerform(usagePoint);
    }

    private UsagePoint doPerform(UsagePoint usagePoint) {
        if (isAllowedToEdit(usagePoint)) {
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
            usagePoint.update();
            return usagePoint;
        }
        throw new WebApplicationException(Response.Status.FORBIDDEN);
    }

    private boolean isAllowedToEdit(UsagePoint usagePoint) {
        return isOwn(usagePoint) || hasEditAllPrivilege();
    }

    private boolean hasEditAllPrivilege() {
        return principal instanceof User && ((User) principal).hasPrivilege("MDC", Privileges.Constants.ADMIN_ANY);
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
