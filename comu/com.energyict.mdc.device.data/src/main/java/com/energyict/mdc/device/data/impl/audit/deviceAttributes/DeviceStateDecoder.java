/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDeviceLifeCycleStatus;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.data.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.device.lifecycle.config.DefaultState;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceStateDecoder {

    private AuditTrailDeviceAtributesDecoder decoder;

    public DeviceStateDecoder(AuditTrailDeviceAtributesDecoder decoder){
        this.decoder = decoder;
    }

    public Optional<AuditLogChange> getAuditLog() {
        List<EndDeviceLifeCycleStatus> allStatuses = getDeviceLifeCycleChangeEvents().stream()
                .sorted(Comparator.comparing(EndDeviceLifeCycleStatus::getCreateTime).reversed())
                .collect(Collectors.toList());
        List<EndDeviceLifeCycleStatus> filteredStatuses = allStatuses.stream()
                .filter(dlcs -> decoder.isBetweenPeriodMod(dlcs.getModTime()))
                .sorted(Comparator.comparing(EndDeviceLifeCycleStatus::getCreateTime).reversed())
                .collect(Collectors.toList());

        if (filteredStatuses.size()==0){
            return Optional.empty();
        }
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(decoder.getDisplayName(PropertyTranslationKeys.DEVICE_STATUS));
        auditLogChange.setType(SimplePropertyType.TEXT.name());

        if (filteredStatuses.size() == 1){
            EndDeviceLifeCycleStatus fromLifeCycle = filteredStatuses.get(0);
            Optional<EndDeviceLifeCycleStatus> toLifeCycle = allStatuses.stream()
                    .filter(endDeviceLifeCycleStatus -> endDeviceLifeCycleStatus.getModTime().isAfter(fromLifeCycle.getModTime()))
                    .min(Comparator.comparing(EndDeviceLifeCycleStatus::getCreateTime));

            auditLogChange.setPreviousValue(getStateName(fromLifeCycle.getState()));
            toLifeCycle.ifPresent(to ->
                    auditLogChange.setValue(getStateName(to.getState()))
            );
        }
        else {
            EndDeviceLifeCycleStatus toLifeCycle = filteredStatuses.get(0);
            EndDeviceLifeCycleStatus fromLifeCycle = filteredStatuses.get(1);
            auditLogChange.setValue(getStateName(toLifeCycle.getState()));
            auditLogChange.setPreviousValue(getStateName(fromLifeCycle.getState()));
        }
        return Optional.of(auditLogChange);
    }

    private String getStateName(State state) {
        return DefaultState
                .from(state)
                .map(s -> decoder.getDeviceLifeCycleConfigurationService().getDisplayName(s))
                .orElseGet(state::getName);
    }

    private List<EndDeviceLifeCycleStatus> getDeviceLifeCycleChangeEvents(){
        DataMapper<EndDeviceLifeCycleStatus> dataMapper = decoder.getOrmService().getDataModel(MeteringService.COMPONENTNAME).get().mapper(EndDeviceLifeCycleStatus.class);
        return dataMapper.select(Condition.TRUE.and(where("endDevice").isEqualTo(decoder.getEndDevice())));
    }
}
