/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.metrologyConfiguration;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.impl.audit.AbstractUsagePointAuditDecoder;
import com.elster.jupiter.metering.impl.config.EffectiveMetrologyContractOnUsagePoint;
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

public class AuditTrailMetrologyDecoder extends AbstractUsagePointAuditDecoder {

    AuditTrailMetrologyDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.setThesaurus(thesaurus);
    }

    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("name", this.getThesaurus().getFormat(PropertyTranslationKeys.USAGEPOINT_GENERAL_INFORMATION).format());
        return builder.build();
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            usagePoint.ifPresent(upEntry -> {
                auditLogChanges.addAll(auditMetrologyConfiguration(upEntry));
                auditLogChanges.addAll(auditPurpose(upEntry));
            });
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> auditMetrologyConfiguration(UsagePoint usagePoint){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        DataMapper<EffectiveMetrologyConfigurationOnUsagePoint> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(EffectiveMetrologyConfigurationOnUsagePoint.class);

        Optional<EffectiveMetrologyConfigurationOnUsagePoint> metrologyConfiguration = getChangedObjects(dataMapper, usagePoint.getId())
                .stream()
                .filter(distinctByKey(p -> ((EffectiveMetrologyConfigurationOnUsagePoint)p).getVersion()))
                .map(p-> (EffectiveMetrologyConfigurationOnUsagePoint)p)
                .sorted(Comparator.comparing(EffectiveMetrologyConfigurationOnUsagePoint::getVersion))
                .reduce((first, second) -> second);

        metrologyConfiguration.ifPresent(
                mc -> {
                    getAuditLogChange(Optional.of(mc.getMetrologyConfiguration().getName()), PropertyTranslationKeys.USAGEPOINT_METROLOGYCONFIGURATION, SimplePropertyType.TEXT).ifPresent(auditLogChanges::add);
                    Optional.ofNullable(mc.getEnd())
                            .ifPresent(date ->
                                    getAuditLogChange(Optional.of(date), PropertyTranslationKeys.USAGEPOINT_METROLOGY_END_DATE, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add)
                            );
                    if (mc.getEnd() == null){
                        Optional.ofNullable(mc.getStart())
                                .ifPresent(date ->
                                        getAuditLogChange(Optional.of(date), PropertyTranslationKeys.USAGEPOINT_METROLOGY_START_DATE, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add)
                                );
                    }
                }
        );
        return auditLogChanges;
    }

    private List<AuditLogChange> auditPurpose(UsagePoint usagePoint) {
        List<AuditLogChange> auditLogChanges = new ArrayList<>();

        DataMapper<EffectiveMetrologyContractOnUsagePoint> dataMapper = ormService.getDataModel(MeteringService.COMPONENTNAME).get().mapper(EffectiveMetrologyContractOnUsagePoint.class);

        usagePoint.getEffectiveMetrologyConfiguration(getAuditTrailReference().getModTimeStart())
                .ifPresent(effectiveMetrologyConfigurationOnUsagePoint -> {
                    List<EffectiveMetrologyContractOnUsagePoint> actualEntriesByModTime = getActualEntries(dataMapper, ImmutableMap.of("metrologyConfiguration", effectiveMetrologyConfigurationOnUsagePoint));
                    List<EffectiveMetrologyContractOnUsagePoint> actualEntriesByCreateTime = getActualEntriesByCreateTime(dataMapper, ImmutableMap.of("metrologyConfiguration", effectiveMetrologyConfigurationOnUsagePoint));
                    List<EffectiveMetrologyContractOnUsagePoint> actualEntries = new ArrayList<>();
                    actualEntries.addAll(actualEntriesByModTime);
                    actualEntries.addAll(actualEntriesByCreateTime);
                    if (actualEntries.size()>0){
                        EffectiveMetrologyContractOnUsagePoint metrologyContract = actualEntries.get(0);
                        getAuditLogChange(Optional.of(metrologyContract.getMetrologyContract().getMetrologyPurpose().getName()), PropertyTranslationKeys.USAGEPOINT_METROLOGY_PURPOSE_NAME, SimplePropertyType.TEXT).ifPresent(auditLogChanges::add);
                        if(metrologyContract.getRange().hasUpperBound()) {
                            getAuditLogChange(Optional.of(metrologyContract.getRange().upperEndpoint()), PropertyTranslationKeys.USAGEPOINT_METROLOGY_PURPOSE_END_DATE, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                        }
                        if (!metrologyContract.getRange().hasUpperBound() && metrologyContract.getRange().hasLowerBound()){
                            getAuditLogChange(Optional.of(metrologyContract.getRange().lowerEndpoint()), PropertyTranslationKeys.USAGEPOINT_METROLOGY_PURPOSE_START_DATE, SimplePropertyType.TIMESTAMP).ifPresent(auditLogChanges::add);
                        }
                    }
                });
        //getActualEntries(dataMapper, ImmutableMap.of("metrologyConfiguration", usagePoint.getEffectiveMetrologyConfiguration(getAuditTrailReference().getModTimeStart()).get()));
        //List<EffectiveMetrologyContractOnUsagePoint> actualEntries = getActualEntries(dataMapper, ImmutableMap.of("metrologyConfiguratin.USAGEPOINT", usagePoint.getId()));

        /*Optional<EffectiveMetrologyContractOnUsagePoint> changes = getChangedObjects(dataMapper, usagePoint.getId())
                .stream()
                .filter(distinctByKey(p -> ((EffectiveMetrologyContractOnUsagePoint)p).getVersion()))
                .map(p-> (EffectiveMetrologyContractOnUsagePoint)p)
                .sorted(Comparator.comparing(EffectiveMetrologyContractOnUsagePoint::getVersion))
                .reduce((first, second) -> second);
        */

        return auditLogChanges;
    }

    protected Map<String, Object> getActualClauses(long id) {
        return ImmutableMap.of("USAGEPOINT", id);
    }

    protected ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("USAGEPOINT", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    protected ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("USAGEPOINT", id),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Optional<AuditLogChange> getAuditLogChange(Optional to, TranslationKey translationKey, SimplePropertyType simplePropertyType)
    {
        if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT){
            return getAuditLogChangeForOptional(to, translationKey, simplePropertyType);
        }
        return getAuditLogChangeForOptional(to, Optional.empty(), translationKey, simplePropertyType);
    }


}
