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

import com.google.common.collect.ImmutableSetMultimap;

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
        final Condition[] inputCondition = {Condition.TRUE};
        valueMap.entrySet().stream()
                .forEach(entry -> inputCondition[0] = inputCondition[0].and(where(entry.getKey()).isEqualTo(entry.getValue())));

        Condition conditionFromCurrent = inputCondition[0]
                .and(where("modTime").isGreaterThanOrEqual(getAuditTrailReference().getModTimeStart()))
                .and(where("modTime").isLessThanOrEqual(getAuditTrailReference().getModTimeEnd()));
        return dataMapper.select(conditionFromCurrent);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getActualEntriesByCreateTime(DataMapper<T> dataMapper, Map<String, Object> valueMap) {
        final Condition[] inputCondition = {Condition.TRUE};
        valueMap.entrySet().stream()
                .forEach(entry -> inputCondition[0] = inputCondition[0].and(where(entry.getKey()).isEqualTo(entry.getValue())));

        Condition conditionFromCurrent = inputCondition[0]
                .and(where("createTime").isGreaterThanOrEqual(getAuditTrailReference().getModTimeStart()))
                .and(where("createTime").isLessThanOrEqual(getAuditTrailReference().getModTimeEnd()));
        return dataMapper.select(conditionFromCurrent);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getHistoryEntries(DataMapper<T> dataMapper, ImmutableSetMultimap<Operator, Pair<String, Object>> pair) {
        List<Comparison> conditionFromJournal = pair.entries().stream()
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
    public <T> Optional<T> getJournalEntry(DataMapper<T> dataMapper, List<Pair<String, Object>> valueMap) {
        Optional<Pair<String, Object>> versionCount =
            valueMap.stream()
                    .filter(ob -> ob.getFirst().equals("VERSIONCOUNT"))
                    .findFirst();

        if (versionCount.isPresent() && (Long.parseLong(versionCount.get().getLast().toString()) == 0)){
            return Optional.empty();
        }

        List<Comparison> conditionFromJournal = valueMap.stream()
                .map(entry -> Operator.EQUAL.compare(entry.getFirst(), entry.getLast()))
                .collect(Collectors.toList());

        return dataMapper
                .at(Instant.EPOCH)
                .find(conditionFromJournal)
                .stream()
                .min(Comparator.comparing(JournalEntry::getJournalTime))
                .map(JournalEntry::get);
    }

    protected abstract void decodeReference();

    public Optional<AuditLogChange> getAuditLogChangeForString(String from, String to, TranslationKey translationKey) {
        if (!(to == null ? from == null : to.equals(from)))
        {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to);
            auditLogChange.setPreviousValue(from);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> getAuditLogChangeForObject(Object from, Object to, TranslationKey translationKey) {
        if (!(to == null ? from == null : to.equals(from)))
        {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.TEXT.name());
            auditLogChange.setValue(to == null ? "": to.toString());
            auditLogChange.setPreviousValue(from == null ? "": from.toString());
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> getAuditLogChangeForObject(Object from, TranslationKey translationKey) {
        AuditLogChange auditLogChange = new AuditLogChangeBuilder();
        auditLogChange.setName(getDisplayName(translationKey));
        auditLogChange.setType(SimplePropertyType.TEXT.name());
        auditLogChange.setValue(from == null ? "": from.toString());
        return Optional.of(auditLogChange);
    }

    public Optional<AuditLogChange> getAuditLogChangeForBoolean(boolean from, boolean to, TranslationKey translationKey) {
        if (to != from) {
            AuditLogChange auditLogChange = new AuditLogChangeBuilder();
            auditLogChange.setName(getDisplayName(translationKey));
            auditLogChange.setType(SimplePropertyType.BOOLEAN.name());
            auditLogChange.setValue(to);
            auditLogChange.setPreviousValue(from);
            return Optional.of(auditLogChange);
        }
        return Optional.empty();
    }

    public Optional<AuditLogChange> getAuditLogChangeForInteger(Integer from, Integer to, TranslationKey translationKey) {
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

    public Optional<AuditLogChange> getAuditLogChangeForString(String to, TranslationKey translationKey) {
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

    public Thesaurus getThesaurus() {
        return this.thesaurus;
    }

    public boolean isBetweenPeriodMod(Instant instant) {
        return (instant.isAfter(getAuditTrailReference().getModTimeStart()) || instant.equals(getAuditTrailReference().getModTimeStart())) &&
                (instant.isBefore(getAuditTrailReference().getModTimeEnd()) || instant.equals(getAuditTrailReference().getModTimeEnd()));
    }
}
