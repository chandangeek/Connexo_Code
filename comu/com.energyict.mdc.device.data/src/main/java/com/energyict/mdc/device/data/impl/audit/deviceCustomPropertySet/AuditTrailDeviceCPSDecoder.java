/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceCustomPropertySet;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditTrailDeviceCPSDecoder extends AbstractCPSAuditDecoder {

    AuditTrailDeviceCPSDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
        super(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService);
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
                    CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(getCustomPropertySet().get(),
                            isContextObsolete() ? getAuditTrailReference().getModTimeEnd().minusMillis(1) : getAuditTrailReference().getModTimeEnd());
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

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();
            if (!registeredCustomPropertySet.isPresent() || !device.isPresent()) {
                return auditLogChanges;
            }

            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
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
        Device dev = device.get();
        if (registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(registeredCustomPropertySet.getCustomPropertySet(), dev, at, at);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesModifiedBetweenFor(registeredCustomPropertySet.getCustomPropertySet(), dev, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                        .getModTimeEnd());
            }
        } else {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesFor(registeredCustomPropertySet.getCustomPropertySet(), dev, at);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesFor(registeredCustomPropertySet.getCustomPropertySet(), dev);
            }
        }
        return customPropertySetValues;
    }

    protected Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        return getCustomPropertySetFromDeviceType()
                .map(Optional::of)
                .orElseGet(this::getCustomPropertySetFromActive);
    }

    private Optional<RegisteredCustomPropertySet> getCustomPropertySetFromDeviceType() {
        return Optional.ofNullable(device)
                .flatMap(dv -> dv.get().getDeviceType()
                        .getCustomPropertySets().stream()
                        .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                        .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getId() == getAuditTrailReference().getPkContext1())
                        .findFirst()
                );
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