/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.connectionMethods;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditTrailconnectionMethodDecoder extends AbstractCPSAuditDecoder {

    private Optional<ConnectionTask<?, ?>> connectionTask;

    AuditTrailconnectionMethodDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
        super(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService);
    }

    @Override
    protected void decodeReference() {
        try {
            super.decodeReference();
            connectionTask = device
                    .map(dv -> findConnectionTaskOnDevice(dv))
                    .orElseGet(Optional::empty);
        }
        catch (Exception ignored){
        }
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
            auditLogChanges.addAll(getAuditLogFromCPS());
            return auditLogChanges;
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    private List<AuditLogChange> getAuditLogFromCPS(){
        List<AuditLogChange> auditLogChanges = new ArrayList<>();
        Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();

        if (!registeredCustomPropertySet.isPresent() || !device.isPresent() || !connectionTask.isPresent()) {
            return auditLogChanges;
        }

        if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
            CustomPropertySetValues toCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), getAuditTrailReference().getModTimeEnd());
            CustomPropertySetValues fromCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), getAuditTrailReference().getModTimeStart().minusMillis(1));
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
    }

    private Optional<ConnectionTask<?, ?>> findConnectionTaskOnDevice(Device device) {
        return device.getConnectionTasks().stream().filter(c -> c.getId() == getAuditTrailReference().getPkContext1())
                .findFirst();
    }

    private CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        CustomPropertySet customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        ConnectionTask<?, ?> ct = connectionTask.get();
        Long deviceId = device.get().getId();

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