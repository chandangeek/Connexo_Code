/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.connectionMethods;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditTrailconnectionMethodDecoder extends AbstractCPSAuditDecoder {

    private Optional<ConnectionTask<?, ?>> connectionTask = Optional.empty();

    AuditTrailconnectionMethodDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
        super(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService);
    }

    @Override
    protected void decodeReference() {
        try {
            super.decodeReference();
            device.ifPresent(dv -> {
                connectionTask = findConnectionTask(dv);
                if (!connectionTask.isPresent()) {
                    connectionTask = findHistoryConnectionTask();
                }
            });
        }
        catch (Exception ignored){
        }
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    public Object getContextReference() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        connectionTask
                .map(ct -> {
                    builder.put("name", ct.getName());
                    return builder;
         });
        return builder.build();
    }

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            auditLogChanges.addAll(getAuditLogFromCps());
            return auditLogChanges;
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogFromCps(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();
        PartialConnectionTask partialConnectionTask = connectionTask.get().getPartialConnectionTask();

        if (!registeredCustomPropertySet.isPresent() || !device.isPresent() || !connectionTask.isPresent()) {
            return auditLogChanges;
        }

        if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
            List<CustomPropertySetValues> listCustomPropertyValues = customPropertySetService.getListOfValuesModifiedBetweenFor(registeredCustomPropertySet.get()
                    .getCustomPropertySet(), connectionTask.get(), getAuditTrailReference().getModTimeStart(), getAuditTrailReference().getModTimeEnd());

            Optional<CustomPropertySetValues> fromCustomPropertySetValues = listCustomPropertyValues.stream()
                    .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                    .findFirst();
            Optional<CustomPropertySetValues> toCustomPropertySetValues = listCustomPropertyValues.stream()
                    .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                    .skip(1)
                    .findFirst();

            List<CustomPropertySetValues> listHistoryCustomPropertyValues = customPropertySetService.getListValuesHistoryEntityFor(registeredCustomPropertySet.get()
                    .getCustomPropertySet(), connectionTask.get(), getAuditTrailReference().getModTimeStart(), getAuditTrailReference().getModTimeEnd());
            listHistoryCustomPropertyValues = listHistoryCustomPropertyValues.stream().distinct().collect(Collectors.toList());

            if (!fromCustomPropertySetValues.isPresent() && !toCustomPropertySetValues.isPresent()) {
                fromCustomPropertySetValues = listHistoryCustomPropertyValues.stream()
                        .sorted(Comparator.comparing(cpv -> cpv.getEffectiveRange().lowerEndpoint()))
                        .findFirst();
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
                                        getAuditLogChangeForUpdate(finalToCustomPropertySetValues.get(), finalFromCustomPropertySetValues.get(), propertySpec).ifPresent(auditLogChanges::add);
                                    }
                                    else {
                                        getAuditLogChangeFromValues(convertCustomPropertySetValue(finalToCustomPropertySetValues.get(), propertySpec),
                                                partialConnectionTask.getProperty(propertySpec.getName()).getValue().toString(),
                                                propertySpec).ifPresent(auditLogChanges::add);
                                    }
                                }

                        );
            }
        }

        if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
            CustomPropertySetValues toCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), getAuditTrailReference().getModTimeEnd());

            getPropertySpecs()
                    .forEach(propertySpec -> {
                        if (toCustomPropertySetValues.getProperty(propertySpec.getName()) != null) {
                            getAuditLogChangeFromValues(convertCustomPropertySetValue(toCustomPropertySetValues, propertySpec),
                                    partialConnectionTask.getProperty(propertySpec.getName()).getValue().toString(),
                                    propertySpec).ifPresent(auditLogChanges::add);
                        }
                    });
        }
        return auditLogChanges;
    }

    private Optional<ConnectionTask<?, ?>> findConnectionTask(Device device) {
        return device.getConnectionTasks().stream().filter(c -> c.getId() == getAuditTrailReference().getPkContext1())
                .findFirst();
    }

    private Optional<ConnectionTask<?, ?>> findHistoryConnectionTask() {
        DataMapper<ConnectionTask> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(ConnectionTask.class);

        List<ConnectionTask> historyEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(getAuditTrailReference().getPkContext1()));
        List<ConnectionTask<?,?>> historyEntriesExtended = new ArrayList<>();
        if (historyEntries.size()>0){
            historyEntriesExtended.add(historyEntries.get(0));
        }
        return historyEntriesExtended.stream()
                .findFirst();
    }

    private CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        CustomPropertySet customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        ConnectionTask<?, ?> ct = connectionTask.get();

        customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(customPropertySet, ct, at, at);
        if (customPropertySetValues.isEmpty()) {
            customPropertySetValues = customPropertySetService.getUniqueValuesModifiedBetweenFor(customPropertySet, ct, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                    .getModTimeEnd());
        }
        return customPropertySetValues;
    }

    protected Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        return getCustomPropertySetFromActive();
    }

    private Optional<RegisteredCustomPropertySet> getCustomPropertySetFromActive(){
        return customPropertySetService
                .findAllCustomPropertySets()
                .stream()
                .filter(x -> x.getId() == getAuditTrailReference().getPkContext2())
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    protected List<PropertySpec> getPropertySpecs() {
        return connectionTask
                .map(ct -> {
                    return connectionTask.get().getConnectionType().getPropertySpecs();
                })
        .orElseGet(()-> new ArrayList<>());
    }
}