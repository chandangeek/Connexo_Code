package com.elster.jupiter.validation.impl;

import static com.elster.jupiter.util.conditions.Where.where;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.*;
import com.elster.jupiter.validation.MessageSeeds.Constants;

@UniqueStartDate(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.OVERLAPPED_PERIOD + "}")
public final class ValidationRuleSetVersionImpl implements IValidationRuleSetVersion {
    private long id;

    @Size(min = 0, max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    private Instant startDate;
    private Instant endDate;
    private Instant obsoleteTime;

    private Reference<ValidationRuleSet> ruleSet = ValueReference.absent();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Valid
    private List<IValidationRule> rules = new ArrayList<>();
    private List<IValidationRule> rulesToSave = new ArrayList<>();

    private final EventService eventService;
    private final DataModel dataModel;
    private final Provider<ValidationRuleImpl> validationRuleProvider;

    @Inject
    ValidationRuleSetVersionImpl(DataModel dataModel, EventService eventService, Provider<ValidationRuleImpl> validationRuleProvider) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationRuleProvider = validationRuleProvider;
    }

    ValidationRuleSetVersionImpl init(ValidationRuleSet validationRuleSet) {
        return init(validationRuleSet, null, null);
    }

    ValidationRuleSetVersionImpl init(ValidationRuleSet validationRuleSet, String description, Instant startDate) {
        this.ruleSet.set(validationRuleSet);
        this.description = description;
        this.startDate = startDate;
        return this;
    }



    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    @Override
    public Instant getEndDate() {
        return endDate;
    }

    @Override
    public Instant getNotNullStartDate() {
        return Optional.ofNullable(startDate).orElse(Instant.EPOCH);
    }

    @Override
    public Instant getNotNullEndDate() {
        return Optional.ofNullable(endDate).orElse(Instant.MAX);
    }

    @Override
    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }
    @Override
    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public ValidationVersionStatus getStatus() {
        if(Instant.now().isBefore(getNotNullStartDate()))
            return ValidationVersionStatus.FUTURE;
        if(Instant.now().isAfter(getNotNullEndDate()))
            return ValidationVersionStatus.PREVIOUS;
        return ValidationVersionStatus.CURRENT;
    }

    Instant getCreateTime() {
        return createTime;
    }

    Instant getModTime() {
        return modTime;
    }

    String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationRuleSetVersion)) {
            return false;
        }

        ValidationRuleSetVersion validationRuleSetVersion = (ValidationRuleSetVersion) o;

        return id == validationRuleSetVersion.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        addNewRules();
        if (getId() == 0) {
            doPersist();
        } else {
            doUpdate();
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        doGetRules().forEach( rule -> Save.UPDATE.save(dataModel, rule));
        eventService.postEvent(EventType.VALIDATIONRULESETVERSION_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.validate(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULESETVERSION_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        this.setObsoleteTime(Instant.now()); // mark obsolete
        doGetRules().forEach(rule -> rule. delete());
        validationRuleSetVersionFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULESETVERSION_DELETED.topic(), this);
    }

    @Override
    public List<IValidationRule> getRules() {
        return doGetRules()
                .sorted(Comparator.comparing(rule -> rule.getName().toUpperCase()))
                .collect(Collectors.toList());
    }

    private void addNewRules() {
        rulesToSave.forEach( newRule -> {
            Save.CREATE.validate(dataModel, newRule);
            rules.add(newRule);
        });
        rulesToSave.clear();
    }

    private Stream<IValidationRule> doGetRules() {
        return rules.stream().filter(rule -> !rule.isObsolete());
    }

    public List<IValidationRule> getRules(int start, int limit) {
        return Collections.unmodifiableList(
                getRuleQuery().select(
                        Where.where("ruleSetVersion").isEqualTo(this),
                        new Order[]{Order.ascending("name").toUpperCase()},
                        false,
                        new String[]{},
                        start + 1,
                        start + limit));
    }

    private QueryExecutor<IValidationRule> getRuleQuery() {
        QueryExecutor<IValidationRule> ruleQuery = dataModel.query(IValidationRule.class, ValidationRuleProperties.class);
        ruleQuery.setRestriction(where("obsoleteTime").isNull());
        return ruleQuery;
    }

    @Override
    public IValidationRule addRule(ValidationAction action, String implementation, String name) {
        ValidationRuleImpl newRule = validationRuleProvider.get().init(ruleSet.get(), this, action, implementation, name);
        rulesToSave.add(newRule);
        return newRule;
    }

    @Override
    public IValidationRule updateRule(long id, String name, boolean activeStatus, ValidationAction action, List<String> mRIDs, Map<String, Object> properties) {
        IValidationRule rule = getExistingRule(id);
        return doUpdateRule(rule, name, activeStatus, action,  mRIDs, properties);
    }

    @Override
    public IValidationRule cloneRule(IValidationRule iValidationRule){
        IValidationRule newRule = addRule(iValidationRule.getAction(), iValidationRule.getImplementation(), iValidationRule.getName());
        List<String> mRIDs = iValidationRule.getReadingTypes().stream().map(readingTypeInfo -> readingTypeInfo.getMRID()).collect(Collectors.toList());
        updateReadingTypes(newRule, mRIDs);
        Map<String, Object> properties = iValidationRule.getProperties().stream().collect(Collectors.toMap(ValidationRuleProperties::getName, ValidationRuleProperties::getValue));
        newRule.setProperties(properties);
        return newRule;
    }

    private IValidationRule doUpdateRule(IValidationRule rule, String name, boolean activeStatus, ValidationAction action, List<String> mRIDs, Map<String, Object> properties) {
        rule.rename(name);
        rule.setAction(action);

        if (activeStatus != rule.isActive()) {
            rule.toggleActivation();
        }
        updateReadingTypes(rule, mRIDs);
        rule.setProperties(properties);

        return rule;
    }

    private void updateReadingTypes(IValidationRule rule, List<String> mRIDs) {
        rule.clearReadingTypes();
        for (String readingTypeMRID : mRIDs) {
            rule.addReadingType(readingTypeMRID);
        }
    }

    private IValidationRule getExistingRule(long id) {
        return doGetRules()
                .filter(rule -> rule.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The ruleset version" + this.getId() + " doesn't contain provided ruleId: " + id));
    }

    @Override
    public void deleteRule(ValidationRule rule) {
        IValidationRule iRule = (IValidationRule) rule;
        if (doGetRules().anyMatch( candidate -> candidate.equals(iRule))) {
            iRule.delete();
        } else {
            throw new IllegalArgumentException("The rulset version" + this.getId() + " doesn't contain provided ruleId: " + rule.getId());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('\n');
        doGetRules().forEach(rule -> builder.append(rule.toString()).append('\n'));
        return builder.toString();
    }

    private DataMapper<IValidationRuleSetVersion> validationRuleSetVersionFactory() {
        return dataModel.mapper(IValidationRuleSetVersion.class);
    }

    public List<ValidationRule> getRules(Iterable<? extends ReadingType> readingTypes) {
        List<ValidationRule> result = new ArrayList<>();
        List<IValidationRule> rules = getRules();
        for (ValidationRule rule : rules) {
            Set<ReadingType> readingTypesForRule = rule.getReadingTypes();
            for (ReadingType readingtype : readingTypes) {
                if (readingTypesForRule.contains(readingtype)) {
                    result.add(rule);
                }
            }
        }
        return result;
    }

    @Override
    public Instant getObsoleteDate() {
        return getObsoleteTime() != null ? getObsoleteTime() : null;
    }

    private Instant getObsoleteTime() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }


    @Override
    public boolean isObsolete() {
        return getObsoleteDate() != null;
    }

    @Override
    public ValidationRuleSet getRuleSet() {
        return ruleSet.get();
    }

}
