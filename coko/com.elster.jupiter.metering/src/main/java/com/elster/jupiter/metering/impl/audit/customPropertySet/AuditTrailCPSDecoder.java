/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.customPropertySet;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.audit.AbstractCPSAuditDecoder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditTrailCPSDecoder extends AbstractCPSAuditDecoder {

    AuditTrailCPSDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, CustomPropertySetService customPropertySetService) {
        super(ormService, thesaurus, meteringService, customPropertySetService);
    }

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();

        registeredCustomPropertySet
                .map(set -> builder.put("name", set.getCustomPropertySet().getName()));
        registeredCustomPropertySet
                .filter(set -> set.getCustomPropertySet().isVersioned())
                .ifPresent(set -> {
                    CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(getCustomPropertySet().get(), getAuditTrailReference().getModTimeEnd());
                    if (customPropertySetValues.getEffectiveRange().hasLowerBound()) {
                        builder.put("startTime", customPropertySetValues.getEffectiveRange().lowerEndpoint());
                    }
                    if (customPropertySetValues.getEffectiveRange().hasUpperBound()) {
                        builder.put("endTime", customPropertySetValues.getEffectiveRange().upperEndpoint());
                    }
                    builder.put("isVersioned", true);
                });
        return builder.build();
    }

    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context){
        return getAuditTrailReference().getOperation().equals(UnexpectedNumberOfUpdatesException.Operation.INSERT) ?
                UnexpectedNumberOfUpdatesException.Operation.INSERT : UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        return getAuditLogChangesFromDevice().stream()
                        .distinct()
                        .collect(Collectors.toList());
    }

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();
            if (!registeredCustomPropertySet.isPresent() || !usagePoint.isPresent()) {
                return auditLogChanges;
            }

            if ((getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) ||
                 (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.DELETE)){
                CustomPropertySetValues toCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(),getAuditTrailReference().getModTimeEnd());
                CustomPropertySetValues fromCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), getAuditTrailReference().getModTimeStart()
                        .minusMillis(1));
                getPropertySpecs()
                        .forEach(propertySpec ->
                                getAuditLogChangeForUpdate(toCustomPropertySetValues, fromCustomPropertySetValues, propertySpec).ifPresent(auditLogChanges::add)
                        );
            }

            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
                CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), getAuditTrailReference().getModTimeEnd());
                getPropertySpecs()
                        .forEach(propertySpec ->
                                getAuditLogChangeForInsert(registeredCustomPropertySet.get(), customPropertySetValues, propertySpec).ifPresent(auditLogChanges::add)
                        );
            }
            return auditLogChanges;

        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        UsagePoint up = usagePoint.get();
        if (registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(registeredCustomPropertySet.getCustomPropertySet(), up, at, at);
            if (customPropertySetValues.isEmpty()) {
                /*customPropertySetValues = customPropertySetService.getUniqueValuesModifiedBetweenFor(registeredCustomPropertySet.getCustomPropertySet(), up, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                        .getModTimeEnd());*/
                customPropertySetValues = (CustomPropertySetValues)customPropertySetService.getListOfValuesModifiedBetweenFor(registeredCustomPropertySet.getCustomPropertySet(), up,
                        getAuditTrailReference().getModTimeStart(), getAuditTrailReference().getModTimeEnd()).get(0);
            }
        } else {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesFor(registeredCustomPropertySet.getCustomPropertySet(), up, at);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesFor(registeredCustomPropertySet.getCustomPropertySet(), up);
            }
        }
        return customPropertySetValues;
    }

    protected Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        if (!usagePoint.isPresent()) {
            return Optional.empty();
        }
        return usagePoint.get().forCustomProperties().getAllPropertySets()
                .stream()
                .map(r -> (RegisteredCustomPropertySet) r)
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getId() == getAuditTrailReference().getPkContext1())
                .findFirst();
    }

    private Optional<RegisteredCustomPropertySet> getCustomPropertySetFromActive(){
        return customPropertySetService
                .findActiveCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(x -> x.getId() == getAuditTrailReference().getPkContext1())
                .findFirst();
    }
}