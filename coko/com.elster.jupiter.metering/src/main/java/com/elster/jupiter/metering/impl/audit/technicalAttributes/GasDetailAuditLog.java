/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;

public class GasDetailAuditLog {

    private final GasDetail from;
    private final GasDetail to;
    private final AuditTrailTechnicalAttributesDecoder decoder;

    GasDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, GasDetail from, GasDetail to){
        this.decoder = decoder;
        this.from = from;
        this.to = to;
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            decoder.getAuditLogChangeForString(from.isGrounded().toString(), to.isGrounded().toString(), PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isLimiter().toString(), to.isLimiter().toString(), PropertyTranslationKeys.USAGEPOINT_LIMITER).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getLoadLimiterType(), to.getLoadLimiterType(), PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getLoadLimit().toString(), to.getLoadLimit().toString(), PropertyTranslationKeys.USAGEPOINT_LOADLIMIT).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getPhysicalCapacity().toString(), to.getPhysicalCapacity().toString(), PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getPressure().toString(), to.getPressure().toString(), PropertyTranslationKeys.USAGEPOINT_PRESSURE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isBypassInstalled().toString(), to.isBypassInstalled().toString(), PropertyTranslationKeys.USAGEPOINT_BYPASS).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getBypassStatus().toString(), to.getBypassStatus().toString(), PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isValveInstalled().toString(), to.isValveInstalled().toString(), PropertyTranslationKeys.USAGEPOINT_VALVE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isCapped().toString(), to.isCapped().toString(), PropertyTranslationKeys.USAGEPOINT_CAPPED).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isClamped().toString(), to.isClamped().toString(), PropertyTranslationKeys.USAGEPOINT_CLAMPED).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isInterruptible().toString(), to.isInterruptible().toString(), PropertyTranslationKeys.USAGEPOINT_INTERRUPTABLE).ifPresent(auditLogChanges::add);
        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
