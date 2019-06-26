/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GasDetailAuditLog {

    private Optional<GasDetail> from = Optional.empty();
    private Optional<GasDetail> to = Optional.empty();
    private final AuditTrailTechnicalAttributesDecoder decoder;

    GasDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, GasDetail from, GasDetail to){
        this.decoder = decoder;
        this.from = Optional.of(from);
        this.to = Optional.of(to);
    }

    GasDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, GasDetail from){
        this.decoder = decoder;
        this.from = Optional.of(from);
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            if (from.isPresent() && to.isPresent()) {
                GasDetail fromEntity = from.get();
                GasDetail toEntity = to.get();

                decoder.getAuditLogChangeForObject(fromEntity.isGrounded(), toEntity.isGrounded(), PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isLimiter(), toEntity.isLimiter(), PropertyTranslationKeys.USAGEPOINT_LIMITER).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForString(fromEntity.getLoadLimiterType(), toEntity.getLoadLimiterType(), PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getLoadLimit(), toEntity.getLoadLimit(), PropertyTranslationKeys.USAGEPOINT_LOADLIMIT).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPhysicalCapacity(), toEntity.getPhysicalCapacity(), PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPressure(), toEntity.getPressure(), PropertyTranslationKeys.USAGEPOINT_PRESSURE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isBypassInstalled(), toEntity.isBypassInstalled(), PropertyTranslationKeys.USAGEPOINT_BYPASS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForBypassStatus(fromEntity.getBypassStatus(), toEntity.getBypassStatus(), PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isValveInstalled(), toEntity.isValveInstalled(), PropertyTranslationKeys.USAGEPOINT_VALVE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isCapped(), toEntity.isCapped(), PropertyTranslationKeys.USAGEPOINT_CAPPED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isClamped(), toEntity.isClamped(), PropertyTranslationKeys.USAGEPOINT_CLAMPED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isInterruptible(), toEntity.isInterruptible(), PropertyTranslationKeys.USAGEPOINT_INTERRUPTABLE).ifPresent(auditLogChanges::add);
            }
            else if (from.isPresent()) {
                GasDetail fromEntity = from.get();

                decoder.getAuditLogChangeForObject(fromEntity.isGrounded(), PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isLimiter(), PropertyTranslationKeys.USAGEPOINT_LIMITER).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForString(fromEntity.getLoadLimiterType(), PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getLoadLimit(), PropertyTranslationKeys.USAGEPOINT_LOADLIMIT).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPhysicalCapacity(), PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPressure(), PropertyTranslationKeys.USAGEPOINT_PRESSURE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isBypassInstalled(), PropertyTranslationKeys.USAGEPOINT_BYPASS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForBypassStatus(fromEntity.getBypassStatus(), PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isValveInstalled(), PropertyTranslationKeys.USAGEPOINT_VALVE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isCapped(), PropertyTranslationKeys.USAGEPOINT_CAPPED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isClamped(), PropertyTranslationKeys.USAGEPOINT_CLAMPED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isInterruptible(), PropertyTranslationKeys.USAGEPOINT_INTERRUPTABLE).ifPresent(auditLogChanges::add);
            }
        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
