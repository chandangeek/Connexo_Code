/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;

import java.util.ArrayList;
import java.util.List;

public class HeatDetailAuditLog {

    private final HeatDetail from;
    private final HeatDetail to;
    private final AuditTrailTechnicalAttributesDecoder decoder;

    HeatDetailAuditLog(AuditTrailTechnicalAttributesDecoder decoder, HeatDetail from, HeatDetail to){
        this.decoder = decoder;
        this.from = from;
        this.to = to;
    }

    public List<AuditLogChange> getLogs()
    {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        try{
            decoder.getAuditLogChangeForString(from.getPhysicalCapacity().toString(), to.getPhysicalCapacity().toString(), PropertyTranslationKeys.USAGEPOINT_PHYSICAL_CAPACITY).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getPressure().toString(), to.getPressure().toString(), PropertyTranslationKeys.USAGEPOINT_PRESSURE).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isBypassInstalled().toString(), to.isBypassInstalled().toString(), PropertyTranslationKeys.USAGEPOINT_BYPASS).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.getBypassStatus().toString(), to.getBypassStatus().toString(), PropertyTranslationKeys.USAGEPOINT_BYPASS_STATUS).ifPresent(auditLogChanges::add);
            decoder.getAuditLogChangeForString(from.isValveInstalled().toString(), to.isValveInstalled().toString(), PropertyTranslationKeys.USAGEPOINT_VALVE).ifPresent(auditLogChanges::add);

        } catch (Exception e) {
        }
        return auditLogChanges;
    }
}
