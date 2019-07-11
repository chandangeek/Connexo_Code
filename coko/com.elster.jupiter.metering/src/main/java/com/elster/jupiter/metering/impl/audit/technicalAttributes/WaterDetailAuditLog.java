/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WaterDetailAuditLog {

    private Optional<WaterDetail> from = Optional.empty();
    private Optional<WaterDetail> to = Optional.empty();
    private final AuditTrailTechnicalAttributesDecoder decoder;

    WaterDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, WaterDetail from, WaterDetail to){
        this.decoder = decoder;
        this.from = Optional.of(from);
        this.to = Optional.of(to);
    }

    WaterDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, WaterDetail from){
        this.decoder = decoder;
        this.from = Optional.of(from);
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            if (from.isPresent() && to.isPresent()) {
                WaterDetail fromEntity = from.get();
                WaterDetail toEntity = to.get();

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
            }
            else if (from.isPresent()) {
                WaterDetail fromEntity = from.get();

                decoder.getAuditLogChangeForObject(fromEntity.isGrounded(),PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
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
            }
        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
