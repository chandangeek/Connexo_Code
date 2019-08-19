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

    protected ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByJournalClauses(Long id) {
        return ImmutableSetMultimap.of(Operator.EQUAL, Pair.of("ID", id),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    protected ImmutableSetMultimap<Operator, Pair<String, Object>> getHistoryByModTimeClauses(Long id) {
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
