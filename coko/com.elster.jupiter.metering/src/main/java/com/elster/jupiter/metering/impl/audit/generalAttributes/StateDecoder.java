/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit.generalAttributes;

import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.impl.UsagePointStateTemporalImpl;
import com.elster.jupiter.metering.impl.search.PropertyTranslationKeys;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.conditions.Condition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public class StateDecoder {

    private AuditTrailGeneralAttributesDecoder decoder;

    public StateDecoder(AuditTrailGeneralAttributesDecoder decoder){
        this.decoder = decoder;
    }

    public Optional<AuditLogChange> getAuditLog() {
        List<UsagePointStateTemporalImpl> allStatuses = getUsagePointLifeCycleChangeEvents().stream()
                .sorted(Comparator.comparing(UsagePointStateTemporalImpl::getCreateTime).reversed())
                .collect(Collectors.toList());
        List<UsagePointStateTemporalImpl> filteredStatuses = allStatuses.stream()
                .filter(dlcs -> decoder.isBetweenPeriodMod(dlcs.getModTime()))
                .sorted(Comparator.comparing(UsagePointStateTemporalImpl::getCreateTime).reversed())
                .collect(Collectors.toList());

        if (filteredStatuses.size()==0){
            return Optional.empty();
        }
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(decoder.getDisplayName(PropertyTranslationKeys.USAGEPOINT_STATE));
        auditLogChange.setType(SimplePropertyType.TEXT.name());

        if (filteredStatuses.size() == 1){
            UsagePointStateTemporalImpl fromLifeCycle = filteredStatuses.get(0);
            Optional<UsagePointStateTemporalImpl> toLifeCycle = allStatuses.stream()
                    .filter(endDeviceLifeCycleStatus -> endDeviceLifeCycleStatus.getModTime().isAfter(fromLifeCycle.getModTime()))
                    .min(Comparator.comparing(UsagePointStateTemporalImpl::getCreateTime));

            auditLogChange.setPreviousValue(getStateName(fromLifeCycle.getState()));
            toLifeCycle.ifPresent(to ->
                    auditLogChange.setValue(getStateName(to.getState()))
            );
        }
        else {
            UsagePointStateTemporalImpl toLifeCycle = filteredStatuses.get(0);
            UsagePointStateTemporalImpl fromLifeCycle = filteredStatuses.get(1);
            auditLogChange.setValue(getStateName(toLifeCycle.getState()));
            auditLogChange.setPreviousValue(getStateName(fromLifeCycle.getState()));
        }
        return Optional.of(auditLogChange);
    }

    private String getStateName(State state) {
        return decoder.getThesaurus().getString(state.getName(), state.getName());
    }

    private List<UsagePointStateTemporalImpl> getUsagePointLifeCycleChangeEvents(){
        List<UsagePointStateTemporalImpl> lifeCycleChangeEvents = new ArrayList<>();
        decoder.getUsagePoint().
                map(up -> {
                    DataMapper<UsagePointStateTemporalImpl> dataMapper = decoder.getOrmService().getDataModel(MeteringService.COMPONENTNAME).get().mapper(UsagePointStateTemporalImpl.class);
                    lifeCycleChangeEvents.addAll(dataMapper.select(Condition.TRUE.and(where("usagePoint").isEqualTo(up))));
                    return lifeCycleChangeEvents;
                });

        return lifeCycleChangeEvents;
    }
}
