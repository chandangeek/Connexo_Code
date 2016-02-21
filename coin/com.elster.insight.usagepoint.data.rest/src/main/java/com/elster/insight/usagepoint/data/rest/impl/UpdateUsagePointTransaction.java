package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.transaction.Transaction;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.time.Clock;
import java.util.Optional;

final class UpdateUsagePointTransaction implements Transaction<UsagePoint> {

    private final UsagePointInfo info;
    private final Principal principal;
    private final MeteringService meteringService;
    private final SecurityContext securityContext;
    private final Clock clock;

    @Inject
    UpdateUsagePointTransaction(UsagePointInfo info, SecurityContext securityContext, MeteringService meteringService, Clock clock) {
        this.info = info;
        this.principal = securityContext.getUserPrincipal();
        this.meteringService = meteringService;
        this.securityContext = securityContext;
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
                usagePoint.setName(info.name);
                usagePoint.setSdp(info.isSdp);
                usagePoint.setVirtual(info.isVirtual);
                usagePoint.setOutageRegion(info.outageRegion);
                usagePoint.setReadRoute(info.readRoute);
                usagePoint.setServicePriority(info.servicePriority);
                usagePoint.setServiceDeliveryRemark(info.serviceDeliveryRemark);
                UsagePointDetail detail = usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, clock.instant());
                usagePoint.addDetail(detail);
                usagePoint.update();
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
        return securityContext.isUserInRole(Privileges.Constants.ADMIN_ANY);
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
