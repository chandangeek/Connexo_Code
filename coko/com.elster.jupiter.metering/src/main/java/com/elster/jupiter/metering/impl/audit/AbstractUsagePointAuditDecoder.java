/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.audit;

import com.elster.jupiter.audit.AbstractAuditDecoder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;

import com.google.common.collect.ImmutableSetMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractUsagePointAuditDecoder extends AbstractAuditDecoder {

    protected volatile OrmService ormService;
    protected volatile MeteringService meteringService;

    protected Optional<UsagePoint> usagePoint = Optional.empty();

    @Override
    public String getName() {
       return usagePoint
                .map(UsagePoint::getName)
                .orElseGet(() -> "");
    }

    @Override
    protected void decodeReference() {
        usagePoint = meteringService.findUsagePointById(getAuditTrailReference().getPkDomain());
    }
/*
    protected Optional<Device> getDeviceFromHistory(long id) {
        DataMapper<Device> dataMapper = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get().mapper(Device.class);
        ImmutableSetMultimap<Operator, Pair<String, Object>> historyClause = ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("ID", id),
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
        return getJournalEntry(dataMapper, Arrays.asList(Pair.of("ID", from.getId()),
                Pair.of("VERSIONCOUNT", version)))
                .map(Optional::of)
                .orElseGet(() -> getToDeviceEntry(from, version + 1, dataMapper));
    }

    protected Optional<EndDevice> getToEndDeviceEntry(EndDevice from, long version, DataMapper<EndDevice> dataMapper) {
        if (version >= endDevice.get().getVersion()) {
            return endDevice;
        }
        return getJournalEntry(dataMapper,
                Arrays.asList(Pair.of("ID", from.getId()), Pair.of("VERSIONCOUNT", version)))
                .map(Optional::of)
                .orElseGet(() -> getToEndDeviceEntry(from, version + 1, dataMapper));
    }
*/
    public ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    public ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    public <T> List<T> getChangedObjects(DataMapper dataMapper, long id)
    {
        List<T> actualEntries = getActualEntries(dataMapper, getActualClauses(id));
        List<T> historyByModTimeEntries = getHistoryEntries(dataMapper, getHistoryByModTimeClauses(id));
        List<T> historyByJournalTimeEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses(id));

        List<T> allEntries = new ArrayList<>();
        allEntries.addAll(actualEntries);
        allEntries.addAll(historyByModTimeEntries);
        allEntries.addAll(historyByJournalTimeEntries);
        return allEntries;
    }

    protected static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    protected Map<String, Object> getActualClauses(long id) {
        return new HashMap<>();
    }
}
