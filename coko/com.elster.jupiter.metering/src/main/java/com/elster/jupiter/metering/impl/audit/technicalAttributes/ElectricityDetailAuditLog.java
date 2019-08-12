/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElectricityDetailAuditLog {

    private Optional<ElectricityDetail> from = Optional.empty();
    private Optional<ElectricityDetail> to = Optional.empty();
    private final AuditTrailTechnicalAttributesDecoder decoder;

    ElectricityDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, ElectricityDetail from, ElectricityDetail to){
        this.decoder = decoder;
        this.from = Optional.of(from);
        this.to = Optional.of(to);
    }

    ElectricityDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, ElectricityDetail from){
        this.decoder = decoder;
        this.from = Optional.of(from);
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            if (from.isPresent() && to.isPresent()) {
                ElectricityDetail fromEntity = from.get();
                ElectricityDetail toEntity = to.get();

                decoder.getAuditLogChangeForObject(fromEntity.isGrounded(), toEntity.isGrounded(), PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getNominalServiceVoltage(), toEntity.getNominalServiceVoltage(), PropertyTranslationKeys.USAGEPOINT_NOMINALVOLTAGE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPhaseCode(), toEntity.getPhaseCode(), PropertyTranslationKeys.USAGEPOINT_PHASECODE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getRatedPower(), toEntity.getRatedPower(), PropertyTranslationKeys.USAGEPOINT_RATEDPOWER).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getRatedCurrent(), toEntity.getRatedCurrent(), PropertyTranslationKeys.USAGEPOINT_RATEDCURRENT).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getEstimatedLoad(), toEntity.getEstimatedLoad(), PropertyTranslationKeys.USAGEPOINT_ESTIMATEDLOAD).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isLimiter(), toEntity.isLimiter(), PropertyTranslationKeys.USAGEPOINT_LIMITER).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForString(fromEntity.getLoadLimiterType(), toEntity.getLoadLimiterType(), PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getLoadLimit(), toEntity.getLoadLimit(), PropertyTranslationKeys.USAGEPOINT_LOADLIMIT).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isInterruptible(), toEntity.isInterruptible(), PropertyTranslationKeys.USAGEPOINT_INTERRUPTABLE).ifPresent(auditLogChanges::add);
            }
            else if (from.isPresent()){
                ElectricityDetail fromEntity = from.get();

                decoder.getAuditLogChangeForObject(fromEntity.isGrounded(), PropertyTranslationKeys.USAGEPOINT_GROUNDED).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getNominalServiceVoltage(), PropertyTranslationKeys.USAGEPOINT_NOMINALVOLTAGE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPhaseCode(), PropertyTranslationKeys.USAGEPOINT_PHASECODE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getRatedPower(), PropertyTranslationKeys.USAGEPOINT_RATEDPOWER).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getRatedCurrent(), PropertyTranslationKeys.USAGEPOINT_RATEDCURRENT).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getEstimatedLoad(), PropertyTranslationKeys.USAGEPOINT_ESTIMATEDLOAD).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isLimiter(), PropertyTranslationKeys.USAGEPOINT_LIMITER).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForString(fromEntity.getLoadLimiterType(), PropertyTranslationKeys.USAGEPOINT_LOAD_LIMITER_TYPE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getLoadLimit(), PropertyTranslationKeys.USAGEPOINT_LOADLIMIT).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isInterruptible(), PropertyTranslationKeys.USAGEPOINT_INTERRUPTABLE).ifPresent(auditLogChanges::add);
            }

        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
