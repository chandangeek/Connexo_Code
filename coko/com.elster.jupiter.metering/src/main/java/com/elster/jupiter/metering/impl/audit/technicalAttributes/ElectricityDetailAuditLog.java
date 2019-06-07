/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;

public class ElectricityDetailAuditLog {

    private final ElectricityDetail from;
    private final ElectricityDetail to;
    private final AuditTrailTechnicalAttributesDecoder decoder;

    ElectricityDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, ElectricityDetail from, ElectricityDetail to){
        this.decoder = decoder;
        this.from = from;
        this.to = to;
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            decoder.getAuditLogChangeForString(from.isGrounded().toString(), to.isGrounded().toString(), PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getNominalServiceVoltage().toString(), to.getNominalServiceVoltage().toString(), PropertyTranslationKeys.USAGEPOINT_NOMINALVOLTAGE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getPhaseCode().toString(), to.getPhaseCode().toString(), PropertyTranslationKeys.USAGEPOINT_PHASECODE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getRatedPower().toString(), to.getRatedPower().toString(), PropertyTranslationKeys.USAGEPOINT_RATEDPOWER).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getRatedCurrent().toString(), to.getRatedCurrent().toString(), PropertyTranslationKeys.USAGEPOINT_RATEDCURRENT).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getEstimatedLoad().toString(), to.getEstimatedLoad().toString(), PropertyTranslationKeys.USAGEPOINT_ESTIMATEDLOAD).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isLimiter().toString(), to.isLimiter().toString(), PropertyTranslationKeys.USAGEPOINT_LIMITER).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getLoadLimiterType(), to.getLoadLimiterType(), PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getLoadLimit().toString(), to.getLoadLimit().toString(), PropertyTranslationKeys.USAGEPOINT_LOADLIMIT).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isInterruptible().toString(), to.isInterruptible().toString(), PropertyTranslationKeys.USAGEPOINT_INTERRUPTABLE).ifPresent(auditLogChanges::add);
        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
