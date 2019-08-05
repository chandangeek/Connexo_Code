/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.technicalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.impl.audit.AbstractUsagePointAuditDecoder;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

                List<UsagePointDetail> actualEntries = getActualEntries(dataMapper, getActualClauses(upEntry.getId()));
                List<UsagePointDetail> historyByModTimeEntries = getHistoryEntries(dataMapper, getHistoryByModTimeClauses(upEntry.getId()));

                List<UsagePointDetail> allEntries = new ArrayList<>();
                allEntries.addAll(actualEntries);
                allEntries.addAll(historyByModTimeEntries);

                allEntries = allEntries
                        .stream()
                        .sorted(Comparator.comparing(mc -> mc.getInterval().toOpenClosedRange().lowerEndpoint()))
                        .collect(Collectors.toList());

                if (allEntries.size() > 1)
                {
                    UsagePointDetail from = allEntries.get(0);
                    UsagePointDetail to = allEntries.get(allEntries.size() - 1);

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
                DataMapper<UsagePointDetail> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(UsagePointDetail.class);
                List<UsagePointDetail> actualEntries = getChangedObjects(dataMapper, upEntry.getId());
                UsagePointDetail updEntry = actualEntries.get(0);
                getAuditLogChangeForString(updEntry.isCollarInstalled().toString(), PropertyTranslationKeys.USAGEPOINT_COLLAR).ifPresent(auditLogChanges::add);
                if (updEntry instanceof ElectricityDetail) {
                    auditLogChanges.addAll(new ElectricityDetailAuditLog(this, (ElectricityDetail) updEntry).getLogs());
                } else if (updEntry instanceof GasDetail) {
                    auditLogChanges.addAll(new GasDetailAuditLog(this, (GasDetail) updEntry).getLogs());
                } else if (updEntry instanceof WaterDetail) {
                    auditLogChanges.addAll(new WaterDetailAuditLog(this, (WaterDetail) updEntry).getLogs());
                } else if (updEntry instanceof HeatDetail) {
                    auditLogChanges.addAll(new HeatDetailAuditLog(this, (HeatDetail) updEntry).getLogs());
                }

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

    public Optional<AuditLogChange> getAuditLogChangeForBypassStatus(BypassStatus from, BypassStatus to, TranslationKey translationKey) {
        if (!(to == null ? from == null : to.equals(from)))
        {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to == null ? "": to.getDisplayValue(getThesaurus()));
            auditLogChange.setPreviousValue(from == null ? "": from.getDisplayValue(getThesaurus()));
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> getAuditLogChangeForBypassStatus(BypassStatus from, TranslationKey translationKey) {
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(getDisplayName(translationKey));
        auditLogChange.setType(SimplePropertyType.TEXT.name());
        auditLogChange.setValue(from == null ? "": from.getDisplayValue(getThesaurus()));
        return Optional.of(auditLogChange);
    }
}
