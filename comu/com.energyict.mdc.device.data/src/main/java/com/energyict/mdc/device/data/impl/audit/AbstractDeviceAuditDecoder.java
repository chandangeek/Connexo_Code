/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AbstractAuditDecoder;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import com.google.common.collect.ImmutableMap;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDeviceAuditDecoder extends AbstractAuditDecoder {

    protected volatile OrmService ormService;
    protected volatile ServerDeviceService serverDeviceService;
    protected volatile MeteringService meteringService;

    protected Optional<Device> device = Optional.empty();
    protected Optional<EndDevice> endDevice = Optional.empty();

    @Override
    public String getName() {
        return device
                .map(Device::getName)
                .orElseGet(() -> "");
    }

    @Override
    protected void decodeReference() {
        meteringService.findEndDeviceById(getAuditTrailReference().getPkDomain())
                .ifPresent(ed -> {
                    endDevice = Optional.of(ed);
                    device = serverDeviceService.findDeviceById(Long.parseLong(ed.getAmrId()))
                            .map(Optional::of)
                            .orElseGet(() -> {
                                isRemoved = true;
                                return getDeviceFromHistory(Long.parseLong(ed.getAmrId()));
                            });
                });
    }

    protected Optional<Device> getDeviceFromHistory(long id) {
        DataMapper<Device> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(Device.class);
        Map<Operator, Pair<String, Object>> historyClause = ImmutableMap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journaltime", getAuditTrailReference().getModTimeStart()));

        return getHistoryEntries(dataMapper, historyClause)
                .stream().max(Comparator.comparing(Device::getVersion));
    }

    protected boolean isDomainObsolete() {
        return endDevice.map(ed ->
                ed.getObsoleteTime()
                        .filter(obsoleteTime -> (obsoleteTime.isAfter(getAuditTrailReference().getModTimeStart()) || obsoleteTime.equals(getAuditTrailReference().getModTimeStart())) &&
                                (obsoleteTime.isBefore(getAuditTrailReference().getModTimeEnd()) || obsoleteTime.equals(getAuditTrailReference().getModTimeEnd())))
                        .isPresent())
                .orElse(false);
    }

    protected Optional<Device> getToDeviceEntry(Device from, long version, DataMapper<Device> dataMapper) {
        if (version >= device.get().getVersion()) {
            return device;
        }
        return getJournalEntry(dataMapper, ImmutableMap.of("ID", from.getId(),
                "VERSIONCOUNT", version))
                .map(Optional::of)
                .orElseGet(() -> getToDeviceEntry(from, version + 1, dataMapper));
    }

    protected Optional<EndDevice> getToEndDeviceEntry(EndDevice from, long version, DataMapper<EndDevice> dataMapper) {
        if (version >= endDevice.get().getVersion()) {
            return endDevice;
        }
        return getJournalEntry(dataMapper, ImmutableMap.of("ID", from.getId(),
                "VERSIONCOUNT", version))
                .map(Optional::of)
                .orElseGet(() -> getToEndDeviceEntry(from, version + 1, dataMapper));
    }

    protected Map<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long deviceId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("id", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    protected Map<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long deviceId) {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("ID", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }
}
