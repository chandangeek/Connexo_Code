/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.impl.audit.AbstractUsagePointAuditDecoder;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditTrailTechnicalAttributesDecoder extends AbstractUsagePointAuditDecoder {

    AuditTrailTechnicalAttributesDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.setThesaurus(thesaurus);
    }

    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("name", this.getThesaurus().getFormat(PropertyTranslationKeys.USAGEPOINT_TECHNICAL_INFORMATION).format());
        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
                return getAuditLogChangesForUpdate();
            } else if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
                return getAuditLogChangesForNew();
            }
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesForUpdate() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            usagePoint.ifPresent(upEntry -> {
                DataMapper<UsagePointDetail> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(UsagePointDetail.class);

                List<UsagePointDetail> allEntries = getChangedObjects(dataMapper, upEntry.getId())
                        .stream()
                        .filter(distinctByKey(p -> ((UsagePointDetail)p).getVersion()))
                        .map(p-> (UsagePointDetail)p)
                        .sorted(Comparator.comparing(UsagePointDetail::getVersion))
                        .collect(Collectors.toList());

                UsagePointDetail from = allEntries.get(0);
                UsagePointDetail to = allEntries.get(allEntries.size() - 1);
                // translate it
                getAuditLogChangeForString(from.isCollarInstalled().toString(), to.isCollarInstalled().toString(), PropertyTranslationKeys.USAGEPOINT_COLLAR).ifPresent(auditLogChanges::add);

                if (from instanceof ElectricityDetail) {
                    auditLogChanges.addAll(new ElectricityDetailAuditLog(this, (ElectricityDetail) from, (ElectricityDetail)to).getLogs());
                } else if (from instanceof GasDetail) {
                    auditLogChanges.addAll(new GasDetailAuditLog(this, (GasDetail) from, (GasDetail)to).getLogs());
                } else if (from instanceof WaterDetail) {
                    auditLogChanges.addAll(new WaterDetailAuditLog(this, (WaterDetail) from, (WaterDetail)to).getLogs());
                } else if (from instanceof HeatDetail) {
                    auditLogChanges.addAll(new HeatDetailAuditLog(this, (HeatDetail) from, (HeatDetail)to).getLogs());
                }
            });
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogChangesForNew() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            usagePoint.ifPresent(upEntry -> {
                getAuditLogChangeForString(upEntry.getName(), PropertyTranslationKeys.USAGEPOINT_NAME).ifPresent(auditLogChanges::add);

            });
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    protected Map<String, Object> getActualClauses(long id) {
        return ImmutableMap.of("USAGEPOINTID", id);
    }

    public ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("USAGEPOINTID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    public ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("USAGEPOINTID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }


}
