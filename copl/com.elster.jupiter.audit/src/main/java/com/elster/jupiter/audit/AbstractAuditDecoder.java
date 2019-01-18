package com.elster.jupiter.audit;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

public abstract class AbstractAuditDecoder implements AuditDecoder {

    private AuditTrailReference reference;

    public AbstractAuditDecoder init(AuditTrailReference reference) {
        this.reference = reference;
        decodeReference();
        return this;
    }

    public AuditTrailReference getAuditTrailReference() {
        return reference;
    }

    @Override
    public Object getReference() {
        return new Object();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAffected(DataMapper<T> dataMapper, Map<String, Object> valueMap) {

        Condition inputCondition = Condition.TRUE;
        valueMap.entrySet().stream()
                .forEach(entry -> inputCondition.and(where(entry.getKey()).isEqualTo(entry.getValue())));

        Condition conditionFromCurrent = inputCondition
                .and(where("modTime").isGreaterThanOrEqual(getAuditTrailReference().getModTimeStart()))
                .and(where("modTime").isLessThanOrEqual(getAuditTrailReference().getModTimeEnd()));
        List<T> actualEntries = dataMapper.select(conditionFromCurrent);

        List<Comparison> conditionFromJournal = valueMap.entrySet().stream()
                .map(entry -> Operator.EQUAL.compare(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        conditionFromJournal.add(Operator.GREATERTHANOREQUAL.compare("modTime", getAuditTrailReference().getModTimeStart()));
        conditionFromJournal.add(Operator.LESSTHANOREQUAL.compare("modTime", getAuditTrailReference().getModTimeEnd()));

        List<T> journalEntries = dataMapper
                .at(Instant.EPOCH)
                .find(conditionFromJournal)
                .stream()
                .sorted(Comparator.comparing(JournalEntry::getJournalTime))
                .map(JournalEntry::get)
                .collect(Collectors.toList());

        actualEntries.addAll(journalEntries);
        return new LinkedList<T>(actualEntries);
    }

    public <T> Optional<T> getJournalEntry(DataMapper<T> dataMapper, Map<String, Object> valueMap) {
        if (valueMap.containsKey("VERSIONCOUNT") && (Long.parseLong(valueMap.get("VERSIONCOUNT").toString()) == 0)) {
            return Optional.empty();
        }
        List<Comparison> conditionFromJournal = valueMap.entrySet().stream()
                .map(entry -> Operator.EQUAL.compare(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return dataMapper
                .at(Instant.EPOCH)
                .find(conditionFromJournal)
                .stream()
                .min(Comparator.comparing(JournalEntry::getJournalTime))
                .map(JournalEntry::get);
    }

    protected abstract void decodeReference();
}
