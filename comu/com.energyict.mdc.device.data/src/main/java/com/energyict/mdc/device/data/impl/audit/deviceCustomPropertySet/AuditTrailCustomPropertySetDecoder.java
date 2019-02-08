/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceCustomPropertySet;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.InstantFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractDeviceAuditDecoder;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditTrailCustomPropertySetDecoder extends AbstractDeviceAuditDecoder {

    private final CustomPropertySetService customPropertySetService;

    AuditTrailCustomPropertySetDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
        this.ormService = ormService;
        this.meteringService = meteringService;
        this.serverDeviceService = serverDeviceService;
        this.customPropertySetService = customPropertySetService;
        this.setThesaurus(thesaurus);
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        return isContextObsolete() ? new ArrayList<>() :
                getAuditLogChangesFromDevice().stream()
                        .distinct()
                        .collect(Collectors.toList());
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
                    CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(getCustomPropertySet().get(), device.get(),
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

    private boolean isContextObsolete() {
        return getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.DELETE;
    }

    private List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();
            if (!registeredCustomPropertySet.isPresent() || !device.isPresent()) {
                return auditLogChanges;
            }

            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
                CustomPropertySetValues toCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), device.get(), getAuditTrailReference().getModTimeEnd());
                CustomPropertySetValues fromCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), device.get(), getAuditTrailReference().getModTimeStart()
                        .minusMillis(1));
                getPropertySpecs()
                        .forEach(propertySpec ->
                                getAuditLogChange(toCustomPropertySetValues, fromCustomPropertySetValues, propertySpec).ifPresent(auditLogChanges::add)
                        );
            }

            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
                CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), device.get(), getAuditTrailReference().getModTimeEnd());
                getPropertySpecs()
                        .forEach(propertySpec ->
                                getAuditLogChange(customPropertySetValues, propertySpec).ifPresent(auditLogChanges::add)
                        );
            }
            return auditLogChanges;

        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Device device, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        if (registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(registeredCustomPropertySet.getCustomPropertySet(), device, at, at);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesModifiedBetweenFor(registeredCustomPropertySet.getCustomPropertySet(), device, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                        .getModTimeEnd());
            }
        } else {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device, at);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesFor(registeredCustomPropertySet.getCustomPropertySet(), device);
            }
        }
        return customPropertySetValues;
    }

    private Optional<AuditLogChange> getAuditLogChange(CustomPropertySetValues toCustomPropertySetValues, CustomPropertySetValues fromCustomPropertySetValues, PropertySpec propertySpec) {
        Object toValue = convertCustomPropertySetValue(toCustomPropertySetValues, propertySpec);
        Object fromValue = convertCustomPropertySetValue(fromCustomPropertySetValues, propertySpec);
        if (!toValue.equals(fromValue)) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(propertySpec.getDisplayName());
            auditLogChange.setType(convertCustomPropertySetType(propertySpec));
            auditLogChange.setValue(toValue);
            auditLogChange.setPreviousValue(fromValue);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    private Optional<AuditLogChange> getAuditLogChange(CustomPropertySetValues customPropertySetValues, PropertySpec propertySpec) {
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(propertySpec.getDisplayName());
        auditLogChange.setType(convertCustomPropertySetType(propertySpec));
        auditLogChange.setValue(convertCustomPropertySetValue(customPropertySetValues, propertySpec));
        return Optional.of(auditLogChange);
    }

    private Object convertCustomPropertySetValue(CustomPropertySetValues value, PropertySpec propertySpec) {
        String propertyName = propertySpec.getName();
        if (propertySpec.getValueFactory() instanceof InstantFactory) {
            return Optional.ofNullable(value.getProperty(propertyName)).orElseGet(() -> Instant.EPOCH);
        }
        return Optional.ofNullable(value.getProperty(propertyName))
                .map(Object::toString)
                .orElseGet(() -> "");
    }

    private String convertCustomPropertySetType(PropertySpec propertySpec) {
        if (propertySpec.getValueFactory() instanceof InstantFactory) {
            return SimplePropertyType.TIMESTAMP.toString();
        } else if (propertySpec.getValueFactory() instanceof BooleanFactory) {
            return SimplePropertyType.BOOLEAN.toString();
        }
        return propertySpec.getValueFactory().toString();
    }

    private Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        return Optional.ofNullable(device)
                .flatMap(dev -> dev.get().getDeviceType()
                        .getCustomPropertySets().stream()
                        .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                        .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getId() == getAuditTrailReference().getPkContext())
                        .findFirst());
    }

    @SuppressWarnings("unchecked")
    private List<PropertySpec> getPropertySpecs() {
        return getCustomPropertySet()
                .map(RegisteredCustomPropertySet -> RegisteredCustomPropertySet
                        .getCustomPropertySet().getPropertySpecs())
                .orElseGet(Collections::emptyList);
    }
}