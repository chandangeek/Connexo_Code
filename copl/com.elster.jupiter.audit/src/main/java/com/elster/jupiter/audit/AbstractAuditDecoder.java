package com.elster.jupiter.audit;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.util.Pair;
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
    protected boolean isRemoved = false;
    private volatile Thesaurus thesaurus;

    public AbstractAuditDecoder init(AuditTrailReference reference) {
        this.reference = reference;
        decodeReference();
        return this;
    }

    public AuditTrailReference getAuditTrailReference() {
        return reference;
    }

    @Override
    public Object getContextReference() {
        return "";
    }

    @Override
    public boolean isRemoved() {
        return isRemoved;
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        return operation;
    }

    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
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
        conditionFromJournal.add(Operator.GREATERTHANOREQUAL.compare("journalTime", getAuditTrailReference().getModTimeStart()));
        conditionFromJournal.add(Operator.LESSTHANOREQUAL.compare("journalTime", getAuditTrailReference().getModTimeEnd()));

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

    @SuppressWarnings("unchecked")
    public <T> List<T> getActualEntries(DataMapper<T> dataMapper, Map<String, Object> valueMap) {

        Condition inputCondition = Condition.TRUE;
        valueMap.entrySet().stream()
                .forEach(entry -> inputCondition.and(where(entry.getKey()).isEqualTo(entry.getValue())));

        Condition conditionFromCurrent = inputCondition
                .and(where("modTime").isGreaterThanOrEqual(getAuditTrailReference().getModTimeStart()))
                .and(where("modTime").isLessThanOrEqual(getAuditTrailReference().getModTimeEnd()));
        return dataMapper.select(conditionFromCurrent);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getHistoryEntries(DataMapper<T> dataMapper, Map<Operator, Pair<String, Object>> pair) {

        List<Comparison> conditionFromJournal = pair.entrySet().stream()
                .map(entry -> entry.getKey().compare(entry.getValue().getFirst(), entry.getValue().getLast()))
                .collect(Collectors.toList());

        return dataMapper
                .at(Instant.EPOCH)
                .find(conditionFromJournal)
                .stream()
                .sorted(Comparator.comparing(JournalEntry::getJournalTime))
                .map(JournalEntry::get)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
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


    protected Optional<AuditLogChange> getAuditLogChangeForString(String from, String to, TranslationKey translationKey) {
        if (to.compareTo(from) != 0) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to);
            auditLogChange.setPreviousValue(from);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    protected Optional<AuditLogChange> getAuditLogChangeForInteger(Integer from, Integer to, TranslationKey translationKey) {
        if (to.compareTo(from) != 0) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.INTEGER.name());
            auditLogChange.setValue(to);
            auditLogChange.setPreviousValue(from);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    protected Optional<AuditLogChange> getAuditLogChangeForOptional(Optional from, Optional to, TranslationKey translationKey) {
        if (!compareOptionals(to, from)) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            to.ifPresent(dt -> auditLogChange.setValue(dt));
            from.ifPresent(dt -> auditLogChange.setPreviousValue(dt));
            auditLogChange.setValue(to.get());
            auditLogChange.setPreviousValue(from.get());
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    protected Optional<AuditLogChange> getAuditLogChangeForOptional(Optional from, Optional to, TranslationKey translationKey, SimplePropertyType simplePropertyType) {
        if (!compareOptionals(to, from)) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(simplePropertyType.name());
            to.ifPresent(auditLogChange::setValue);
            from.ifPresent(auditLogChange::setPreviousValue);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    protected Optional<AuditLogChange> getAuditLogChangeForInteger(Integer to, TranslationKey translationKey) {
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(getDisplayName(translationKey));
        auditLogChange.setType(SimplePropertyType.INTEGER.name());
        auditLogChange.setValue(to);
        return Optional.of(auditLogChange);
    }

    protected Optional<AuditLogChange> getAuditLogChangeForString(String to, TranslationKey translationKey) {
        return Optional.ofNullable(to).filter(s -> !s.isEmpty()).map(value -> {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to);
            return auditLogChange;
        });
    }

    protected Optional<AuditLogChange> getAuditLogChangeForOptional(Optional to, TranslationKey translationKey, SimplePropertyType simplePropertyType) {
        return to.map(value -> {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(simplePropertyType.name());
            auditLogChange.setValue(value);
            return auditLogChange;
        });
    }

    private boolean compareOptionals(Optional optional1, Optional optional2) {
        return ((!optional1.isPresent() && !optional2.isPresent()) ||
                (optional1.isPresent() && optional2.isPresent() && optional1.get().equals(optional2.get())));
    }

    public String getDisplayName(TranslationKey key) {
        return this.thesaurus.getFormat(key).format();
    }
}
