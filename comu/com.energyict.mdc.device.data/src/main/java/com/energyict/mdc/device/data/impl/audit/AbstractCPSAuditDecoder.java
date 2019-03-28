/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDomainContextType;
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
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractCPSAuditDecoder extends AbstractDeviceAuditDecoder {

    public final CustomPropertySetService customPropertySetService;

    public AbstractCPSAuditDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
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
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return getCustomPropertySet()
                .filter(registeredCustomPropertySet -> registeredCustomPropertySet.getCustomPropertySet().isVersioned())
                .map(set -> operation)
                .orElseGet(() ->
                    UnexpectedNumberOfUpdatesException.Operation.UPDATE);
    }

    @Override
    public Object getContextReference() {
        return new Object();
    }

    protected boolean isContextObsolete() {
        return getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.DELETE;
    }

    protected abstract Optional<RegisteredCustomPropertySet> getCustomPropertySet();

    protected abstract List<AuditLogChange> getAuditLogChangesFromDevice();

    public Optional<AuditLogChange> getAuditLogChangeForUpdate(CustomPropertySetValues toCustomPropertySetValues, CustomPropertySetValues fromCustomPropertySetValues, PropertySpec propertySpec) {
        return getAuditLogChangeFromValues(convertCustomPropertySetValue(toCustomPropertySetValues, propertySpec),
                convertCustomPropertySetValue(fromCustomPropertySetValues, propertySpec),
                propertySpec);
    }

    protected Optional<AuditLogChange> getAuditLogChangeForInsert(RegisteredCustomPropertySet registeredCustomPropertySet, CustomPropertySetValues customPropertySetValues, PropertySpec propertySpec) {
        return (registeredCustomPropertySet.getCustomPropertySet().isVersioned() && getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) ?
                getAuditLogChangeFromValues(convertCustomPropertySetValue(customPropertySetValues, propertySpec),
                        propertySpec) :
                getAuditLogChangeFromValues(convertCustomPropertySetValue(customPropertySetValues, propertySpec),
                    convertCustomPropertySetDefaultValue(propertySpec),
                    propertySpec);
    }

    public Optional<AuditLogChange> getAuditLogChangeFromValues(Object toValue, Object fromValue, PropertySpec propertySpec){
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

    private Optional<AuditLogChange> getAuditLogChangeFromValues(Object toValue, PropertySpec propertySpec){
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(propertySpec.getDisplayName());
        auditLogChange.setType(convertCustomPropertySetType(propertySpec));
        auditLogChange.setValue(toValue);
        return Optional.of(auditLogChange);
    }

    public Object convertCustomPropertySetValue(CustomPropertySetValues value, PropertySpec propertySpec) {
        String propertyName = propertySpec.getName();
        if (propertySpec.getValueFactory() instanceof InstantFactory) {
            return Optional.ofNullable(value.getProperty(propertyName)).orElseGet(() -> Instant.EPOCH);
        }
        else  if (propertySpec.getValueFactory() instanceof BooleanFactory) {
            return value.getProperty(propertyName);
        }
        return Optional.ofNullable(value.getProperty(propertyName))
                .map(Object::toString)
                .orElseGet(() -> "");
    }

    private Object convertCustomPropertySetDefaultValue(PropertySpec propertySpec) {
        PropertySpecPossibleValues possibleValue = propertySpec.getPossibleValues();
        Optional<Object> value = Optional.ofNullable(possibleValue)
                .map(PropertySpecPossibleValues::getDefault)
                .map(Optional::of).orElseGet(Optional::empty);

        if (propertySpec.getValueFactory() instanceof InstantFactory) {
            return Optional.of(value).get().orElseGet(() -> Instant.EPOCH);
        }
        else  if (propertySpec.getValueFactory() instanceof BooleanFactory) {
            return Optional.ofNullable(value).map(Optional::get).orElseGet(() ->true);
        }
        return Optional.of(value).get()
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

    @SuppressWarnings("unchecked")
    protected List<PropertySpec> getPropertySpecs() {
        return getCustomPropertySet()
                .map(RegisteredCustomPropertySet -> RegisteredCustomPropertySet
                        .getCustomPropertySet().getPropertySpecs())
                .orElseGet(Collections::emptyList);
    }
}