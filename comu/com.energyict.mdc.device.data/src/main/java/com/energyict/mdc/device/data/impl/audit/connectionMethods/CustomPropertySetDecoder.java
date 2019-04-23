/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.connectionMethods;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditTrailReference;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialConnectionTaskProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomPropertySetDecoder {

    private AuditTrailConnectionMethodDecoder decoder;

    public CustomPropertySetDecoder(AuditTrailConnectionMethodDecoder decoder){
        this.decoder = decoder;
    }

    public List<AuditLogChange> getAuditLogs(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();

        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = decoder.getCustomPropertySet();
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTask();
        PartialConnectionTask partialConnectionTask = connectionTask.get().getPartialConnectionTask();
        Optional<Device> device = decoder.getDevice();

        if (!registeredCustomPropertySet.isPresent() || !device.isPresent() || !connectionTask.isPresent()) {
            return auditLogChanges;
        }

        if (decoder.getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
            auditLogChanges.addAll(fromCustomPropertySetForUpdate(registeredCustomPropertySet.get(), partialConnectionTask));
        }
        else if (decoder.getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
            auditLogChanges.addAll(fromCustomPropertySetForInsert(registeredCustomPropertySet.get(), partialConnectionTask));
        }
        return auditLogChanges;
    }

    private List<AuditLogChange> fromCustomPropertySetForUpdate(RegisteredCustomPropertySet registeredCustomPropertySet, PartialConnectionTask partialConnectionTask){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        Optional<ConnectionTask<?, ?>> connectionTask = getConnectionTask();
        Optional<Device> device = decoder.getDevice();
        List<CustomPropertySetValues> listCustomPropertyValues = getCustomPropertySetService().getListOfValuesModifiedBetweenFor(registeredCustomPropertySet
                .getCustomPropertySet(), connectionTask.get(), decoder.getAuditTrailReference().getModTimeStart(), decoder.getAuditTrailReference().getModTimeEnd());

        Optional<CustomPropertySetValues> fromCustomPropertySetValues = listCustomPropertyValues.stream().min(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()));
        Optional<CustomPropertySetValues> toCustomPropertySetValues = listCustomPropertyValues.stream()
                .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                .skip(1)
                .findFirst();

        List<CustomPropertySetValues> listHistoryCustomPropertyValues = getCustomPropertySetService().getListValuesHistoryEntityFor(registeredCustomPropertySet
                .getCustomPropertySet(), connectionTask.get(), decoder.getAuditTrailReference().getModTimeStart(), decoder.getAuditTrailReference().getModTimeEnd());
        listHistoryCustomPropertyValues = listHistoryCustomPropertyValues.stream().distinct().collect(Collectors.toList());

        if (!fromCustomPropertySetValues.isPresent() && !toCustomPropertySetValues.isPresent()) {
            fromCustomPropertySetValues = listHistoryCustomPropertyValues.stream().min(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()));
            toCustomPropertySetValues = listHistoryCustomPropertyValues.stream()
                    .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                    .skip(1)
                    .findFirst();
        }

        if (fromCustomPropertySetValues.isPresent() && toCustomPropertySetValues.isPresent()){
            Optional<CustomPropertySetValues> finalToCustomPropertySetValues = toCustomPropertySetValues;
            Optional<CustomPropertySetValues> finalFromCustomPropertySetValues = fromCustomPropertySetValues;
            getPropertySpecs()
                    .forEach(propertySpec -> {
                                if (finalFromCustomPropertySetValues.get().getProperty(propertySpec.getName()) != null) {
                                    decoder.getAuditLogChangeForUpdate(finalToCustomPropertySetValues.get(), finalFromCustomPropertySetValues.get(), propertySpec).ifPresent(auditLogChanges::add);
                                }
                                else {
                                    Object toValue = decoder.convertCustomPropertySetValue(finalToCustomPropertySetValues.get(), propertySpec);
                                    if (toValue!= null) {
                                        decoder.getAuditLogChangeFromValues(toValue,
                                                getValueFromPartialConnectionTask(partialConnectionTask, propertySpec),
                                                propertySpec).ifPresent(auditLogChanges::add);
                                    }
                                }
                            }
                    );
        }
        return auditLogChanges;
    }

    private List<AuditLogChange> fromCustomPropertySetForInsert(RegisteredCustomPropertySet registeredCustomPropertySet, PartialConnectionTask partialConnectionTask){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        CustomPropertySetValues toCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet, decoder.getAuditTrailReference().getModTimeEnd());
        getPropertySpecs()
                .forEach(propertySpec -> {
                    if (toCustomPropertySetValues.getProperty(propertySpec.getName()) != null) {
                        decoder.getAuditLogChangeFromValues(decoder.convertCustomPropertySetValue(toCustomPropertySetValues, propertySpec),
                                getValueFromPartialConnectionTask(partialConnectionTask, propertySpec),
                                propertySpec).ifPresent(auditLogChanges::add);
                    }
                });
        return auditLogChanges;
    }


    private CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        CustomPropertySet customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        ConnectionTask<?, ?> ct = getConnectionTask().get();

        customPropertySetValues = getCustomPropertySetService().getUniqueHistoryValuesForVersion(customPropertySet, ct, at, at);
        if (customPropertySetValues.isEmpty()) {
            customPropertySetValues = getCustomPropertySetService().getUniqueValuesModifiedBetweenFor(customPropertySet, ct, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                    .getModTimeEnd());
        }
        return customPropertySetValues;
    }

    private String getValueFromPartialConnectionTask(PartialConnectionTask partialConnectionTask, PropertySpec propertySpec){
        PartialConnectionTaskProperty property = partialConnectionTask.getProperty(propertySpec.getName());
        if (property != null && property.getValue() != null){
            return property.getValue().toString();
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    protected List<PropertySpec> getPropertySpecs() {
        return getConnectionTask()
                .map(ct -> {
                    return getConnectionTask().get().getConnectionType().getPropertySpecs();
                })
                .orElseGet(()-> new ArrayList<>());
    }

    private CustomPropertySetService getCustomPropertySetService(){
        return decoder.getCustomPropertySetService();
    }

    private AuditTrailReference getAuditTrailReference() {
        return decoder.getAuditTrailReference();
    }

    private Optional<ConnectionTask<?, ?>>  getConnectionTask(){
        return decoder.getConnectionTask();
    }
}
