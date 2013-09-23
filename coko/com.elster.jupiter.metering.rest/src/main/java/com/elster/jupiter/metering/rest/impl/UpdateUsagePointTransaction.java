package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.users.User;
import com.google.common.base.Optional;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.Principal;


final class UpdateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;
    private final Principal principal;

    UpdateUsagePointTransaction(UsagePointInfo info, Principal principal) {
        this.info = info;
        this.principal = principal;
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
        if (isAllowedToEdit(usagePoint)) {
            if (usagePoint.getVersion() == info.version) {
                usagePoint.setMRID(info.mRID);
                usagePoint.setAliasName(info.aliasName);
                usagePoint.setDescription(info.description);
                usagePoint.setName(info.name);
                usagePoint.setAmiBillingReady(info.amiBillingReady);
                usagePoint.setCheckBilling(info.checkBilling);
                usagePoint.setConnectionState(info.connectionState);
                usagePoint.setEstimatedLoad(info.estimatedLoad);
                usagePoint.setGrounded(info.grounded);
                usagePoint.setSdp(info.isSdp);
                usagePoint.setVirtual(info.isVirtual);
                usagePoint.setMinimalUsageExpected(info.minimalUsageExpected);
                usagePoint.setNominalServiceVoltage(info.nominalServiceVoltage);
                usagePoint.setOutageRegion(info.outageRegion);
                usagePoint.setPhaseCode(info.phaseCode);
                usagePoint.setRatedCurrent(info.ratedCurrent);
                usagePoint.setRatedPower(info.ratedPower);
                usagePoint.setReadCycle(info.readCycle);
                usagePoint.setReadRoute(info.readRoute);
                usagePoint.setServiceDeliveryRemark(info.serviceDeliveryRemark);
                usagePoint.setServicePriority(info.servicePriority);
                usagePoint.setPhaseCode(info.phaseCode);
                usagePoint.setRatedPower(info.ratedPower);
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
            for (User user : accountability.getParty().getCurrentDelegates()) {
                if (user.equals(principal)) {
                    return true;
                }
            }
        }
        return false;
    }
}
