/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.protocolDialects;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.ProtocolDialectProperties;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AuditTrailDeviceProtocolDialectsDecoder extends AbstractCPSAuditDecoder {


    private Optional<ProtocolDialectProperties> protocolDialectProperties;
    private volatile PropertyValueInfoService propertyValueInfoService;

    AuditTrailDeviceProtocolDialectsDecoder (OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService, PropertyValueInfoService propertyValueInfoService) {
        super(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService);
        this.propertyValueInfoService = propertyValueInfoService;
    }

    @Override
    public String getName() {
        return device
                .map(Device::getName)
                .orElseThrow(() -> new IllegalArgumentException("Device cannot be found"));
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return operation;
    }

    @Override
    protected void decodeReference() {
        super.decodeReference();
        protocolDialectProperties = findProtocolDialectOnDevice(getAuditTrailReference().getPkContext1());
    }

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();

            if (!registeredCustomPropertySet.isPresent() || !device.isPresent() || !protocolDialectProperties.isPresent()) {
                return auditLogChanges;
            }

            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
                List<CustomPropertySetValues> listCustomPropertyValues = customPropertySetService.getListOfValuesModifiedBetweenFor(registeredCustomPropertySet.get()
                        .getCustomPropertySet(), protocolDialectProperties.get(), getAuditTrailReference().getModTimeStart(), getAuditTrailReference().getModTimeEnd());

                Optional<CustomPropertySetValues> fromCustomPropertySetValues = listCustomPropertyValues.stream()
                        .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                        .findFirst();
                Optional<CustomPropertySetValues> toCustomPropertySetValues = listCustomPropertyValues.stream()
                        .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                        .skip(1)
                        .findFirst();

                if (fromCustomPropertySetValues.isPresent() && toCustomPropertySetValues.isPresent()){
                    getPropertySpecs()
                            .forEach(propertySpec ->
                                    getAuditLogChangeForUpdate(toCustomPropertySetValues.get(), fromCustomPropertySetValues.get(), propertySpec).ifPresent(auditLogChanges::add)
                            );
                }
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

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();

        if(protocolDialectProperties.isPresent()) {

            registeredCustomPropertySet
                    .map(set -> builder.put("name", set.getCustomPropertySet().getName()));

            registeredCustomPropertySet
                    .filter(set -> set.getCustomPropertySet().isVersioned())
                    .ifPresent(set -> {
                        CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(),
                                isContextObsolete() ? getAuditTrailReference().getModTimeEnd().minusMillis(1) : getAuditTrailReference().getModTimeEnd());
                        if (customPropertySetValues.getEffectiveRange().hasLowerBound()) {
                            builder.put("startTime", customPropertySetValues.getEffectiveRange().lowerEndpoint());
                        }
                        if (customPropertySetValues.getEffectiveRange().hasUpperBound()) {
                            builder.put("endTime", customPropertySetValues.getEffectiveRange().upperEndpoint());
                        }
                        builder.put("sourceId", protocolDialectProperties.get().getProtocolDialectConfigurationProperties().getId());
                        builder.put("sourceName", protocolDialectProperties.get().getDeviceProtocolDialectName());
                        builder.put("isVersioned", true);
                    });
        }
        return builder.build();
    }


    protected Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        return getCustomPropertySetFromAll();
    }

    private Optional<RegisteredCustomPropertySet> getCustomPropertySetFromAll(){
        return customPropertySetService
                .findAllCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(x -> x.getId() == getAuditTrailReference().getPkContext2())
                .findFirst();
    }

    private Optional<ProtocolDialectProperties> findProtocolDialectOnDevice(long protocolDialectId) {
        for (ProtocolDialectProperties protocolDialectProperty : device.get().getProtocolDialectPropertiesList()) {
            if (protocolDialectProperty.getId() == protocolDialectId) {
                return Optional.of(protocolDialectProperty);
            }
        }
        return  Optional.empty();
    }

    private CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        CustomPropertySet customPropertySet = registeredCustomPropertySet.getCustomPropertySet();

        if (registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(customPropertySet,protocolDialectProperties.get()  , at, at);
            if (customPropertySetValues.isEmpty()) {
                List<CustomPropertySetValues> listCustomPropertyValues = customPropertySetService.getListOfValuesModifiedBetweenFor(customPropertySet, protocolDialectProperties.get(), getAuditTrailReference().getModTimeStart(), getAuditTrailReference().getModTimeEnd());
                if(listCustomPropertyValues.size() > 1) {
                    for (CustomPropertySetValues customValues : listCustomPropertyValues) {
                        if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT && !customValues.getEffectiveRange().hasUpperBound())
                            customPropertySetValues = customValues;
                    }
                } else if (listCustomPropertyValues.size() > 0){
                    customPropertySetValues = listCustomPropertyValues.get(0);
                }
            }
        } else {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesFor(customPropertySet,  protocolDialectProperties.get(), at);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesFor(customPropertySet,  protocolDialectProperties.get());
            }
        }
        return customPropertySetValues;
    }
}