/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.channelCustomPropertySet;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.device.data.impl.audit.AbstractCPSAuditDecoder;

import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AuditTrailChannelCPSDecoder extends AbstractCPSAuditDecoder {

    private Optional<Channel> channel;

    AuditTrailChannelCPSDecoder(OrmService ormService, Thesaurus thesaurus, MeteringService meteringService, ServerDeviceService serverDeviceService, CustomPropertySetService customPropertySetService) {
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
            channel = device
                    .map(dv -> findChannelOnDevice(dv, getAuditTrailReference().getPkContext1()))
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
                    builder.put("sourceId", channel.get().getId());
                    builder.put("sourceName", channel.get().getChannelSpec().getReadingType().getFullAliasName());
                    builder.put("isVersioned", true);
                });
        return builder.build();
    }

    protected List<AuditLogChange> getAuditLogChangesFromDevice() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();
            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = getCustomPropertySet();

            if (!registeredCustomPropertySet.isPresent() || !device.isPresent() || !channel.isPresent()) {
                return auditLogChanges;
            }

            if (getAuditTrailReference().getOperation() == UnexpectedNumberOfUpdatesException.Operation.UPDATE) {
                CustomPropertySetValues toCustomPropertySetValues = getCustomPropertySetValues(registeredCustomPropertySet.get(), getAuditTrailReference().getModTimeEnd());
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

    private Optional<Channel> findChannelOnDevice(Device device, long channelId) {
        return device.getChannels().stream().filter(c -> c.getId() == channelId)
                .findFirst();
    }

    private CustomPropertySetValues getCustomPropertySetValues(RegisteredCustomPropertySet registeredCustomPropertySet, Instant at) {
        CustomPropertySetValues customPropertySetValues;
        CustomPropertySet customPropertySet = registeredCustomPropertySet.getCustomPropertySet();
        ChannelSpec channelSpec = channel.get().getChannelSpec();
        Long deviceId = device.get().getId();

        if (registeredCustomPropertySet.getCustomPropertySet().isVersioned()) {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesForVersion(customPropertySet, channelSpec, at, at, deviceId);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesModifiedBetweenFor(customPropertySet, channelSpec, getAuditTrailReference().getModTimeStart(), getAuditTrailReference()
                        .getModTimeEnd(), deviceId);
            }
        } else {
            customPropertySetValues = customPropertySetService.getUniqueHistoryValuesFor(customPropertySet, channelSpec, at, deviceId);
            if (customPropertySetValues.isEmpty()) {
                customPropertySetValues = customPropertySetService.getUniqueValuesFor(customPropertySet, channelSpec, deviceId);
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