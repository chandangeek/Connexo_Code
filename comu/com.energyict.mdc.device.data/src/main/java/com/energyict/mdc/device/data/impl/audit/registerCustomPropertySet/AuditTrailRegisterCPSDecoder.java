/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.registerCustomPropertySet;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditTrailRegisterCPSDecoder extends AbstractCPSAuditDecoder {

    private Optional<Register> register;

    AuditTrailRegisterCPSDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
        super(ormService, thesaurus, meteringService, serverDeviceService, customPropertySetService);
    }


    @Override
    protected void decodeReference() {
        try {
            device = serverDeviceService.findDeviceById(getAuditTrailReference().getPkDomain())
                    .map(Optional::of)
                    .orElseGet(() -> {
                        isRemoved = true;
                        return getDeviceFromHistory(getAuditTrailReference().getPkDomain());
                    });

            meteringService.findEndDeviceByName(device.get().getName())
                    .ifPresent(ed -> {
                        endDevice = Optional.of(ed);
                    });
            register = device
                    .map(dv -> findRegisterOnDevice(dv, getAuditTrailReference().getPkContext1()))
                    .orElseGet(Optional::empty);
        }
        catch (Exception ignored){
        }
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
                    CustomPropertySetValues customPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(),
                            isContextObsolete() ? getAuditTrailReference().getModTimeEnd().minusMillis(1) : getAuditTrailReference().getModTimeEnd());
                    if (customPropertySetValues.getEffectiveRange().hasLowerBound()) {
                        builder.put("startTime", customPropertySetValues.getEffectiveRange().lowerEndpoint());
                    }
                    if (customPropertySetValues.getEffectiveRange().hasUpperBound()) {
                        builder.put("endTime", customPropertySetValues.getEffectiveRange().upperEndpoint());
                    }
                    builder.put("sourceId", register.get().getRegisterSpecId());
                    builder.put("sourceName", register.get().getRegisterSpec().getReadingType().getFullAliasName());
                    builder.put("isVersioned", true);
                });
        return builder.build();
    }

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();

            if (!registeredCustomPropertySet.isPresent() || !device.isPresent() || !register.isPresent()) {
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
            else if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.INSERT) {
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

    private Optional<Register> findRegisterOnDevice(Device device, long registerId) {
        return device.getRegisters().stream().filter(c -> c.getRegisterSpecId() == registerId)
                .findFirst();
    }

    private CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        CustomPropertySet customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        RegisterSpec registerSpec = register.get().getRegisterSpec();
        Long deviceId = device.get().getId();

        if (registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(customPropertySet, registerSpec, at, at, deviceId);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesModifiedBetweenFor(customPropertySet, registerSpec, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                        .getModTimeEnd(), deviceId);
            }
        } else {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesFor(customPropertySet, registerSpec, at, deviceId);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesFor(customPropertySet, registerSpec, deviceId);
            }
        }
        return customPropertySetValues;
    }

    protected Optional<RegisteredCustomPropertySet> getCustomPropertySet() {
        return getCustomPropertySetFromActive();
    }

    private Optional<RegisteredCustomPropertySet> getCustomPropertySetFromActive(){
        return customPropertySetService
                .findActiveCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .filter(x -> x.getId() == getAuditTrailReference().getPkContext2())
                .findFirst();
    }
}