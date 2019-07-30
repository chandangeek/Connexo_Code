/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeatDetailAuditLog {

    private Optional<HeatDetail> from = Optional.empty();
    private Optional<HeatDetail> to = Optional.empty();
    private final AuditTrailTechnicalAttributesDecoder decoder;

    HeatDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, HeatDetail from, HeatDetail to){
        this.decoder = decoder;
        this.from = Optional.of(from);
        this.to = Optional.of(to);
    }

    HeatDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, HeatDetail from){
        this.decoder = decoder;
        this.from = Optional.of(from);
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            if (from.isPresent() && to.isPresent()) {
                HeatDetail fromEntity = from.get();
                HeatDetail toEntity = to.get();

                decoder.getAuditLogChangeForObject(fromEntity.getPhysicalCapacity(), toEntity.getPhysicalCapacity(), PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPressure(), toEntity.getPressure(), PropertyTranslationKeys.USAGEPOINT_PRESSURE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isBypassInstalled(), toEntity.isBypassInstalled(), PropertyTranslationKeys.USAGEPOINT_BYPASS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForBypassStatus(fromEntity.getBypassStatus(), toEntity.getBypassStatus(), PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isValveInstalled(), toEntity.isValveInstalled(), PropertyTranslationKeys.USAGEPOINT_VALVE).ifPresent(auditLogChanges::add);
            }
            else if (from.isPresent()) {
                HeatDetail fromEntity = from.get();

                decoder.getAuditLogChangeForObject(fromEntity.getPhysicalCapacity(), PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.getPressure(), PropertyTranslationKeys.USAGEPOINT_PRESSURE).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isBypassInstalled(), PropertyTranslationKeys.USAGEPOINT_BYPASS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForBypassStatus(fromEntity.getBypassStatus(), PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS).ifPresent(auditLogChanges::add);
                decoder.getAuditLogChangeForObject(fromEntity.isValveInstalled(), PropertyTranslationKeys.USAGEPOINT_VALVE).ifPresent(auditLogChanges::add);
            }

        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
