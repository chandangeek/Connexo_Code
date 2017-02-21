/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

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
import com.elster.jupiter.validation.EventType;
import com.elster.jupiter.validation.ValidationAction;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleBuilder;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;
import com.elster.jupiter.validation.ValidationVersionStatus;
import com.elster.jupiter.validation.impl.MessageSeeds.Constants;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueStartDate(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.OVERLAPPED_PERIOD + "}")
public final class ValidationRuleSetVersionImpl implements IValidationRuleSetVersion {
    private long id;

    @Size(min = 0, max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    private Instant startDate;
    private transient Instant endDate;
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
    private final Clock clock;

    @Inject
    ValidationRuleSetVersionImpl(DataModel dataModel, EventService eventService, Provider<ValidationRuleImpl> validationRuleProvider, Clock clock) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationRuleProvider = validationRuleProvider;
        this.clock = clock;
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
        if(Instant.now(clock).isBefore(getNotNullStartDate()))
            return ValidationVersionStatus.FUTURE;
        if(Instant.now(clock).isAfter(getNotNullEndDate()))
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
        this.setObsoleteTime(Instant.now(clock)); // mark obsolete
        doGetRules().forEach(IValidationRule::delete);
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
    public ValidationRuleBuilder addRule(ValidationAction action, String implementation, String name) {
        return new ValidationRuleBuilderImpl(this, action, implementation, name);
    }

    IValidationRule newRule(ValidationAction action, String implementation, String name) {
        ValidationRuleImpl newRule = validationRuleProvider.get().init(this, action, implementation, name);
        rulesToSave.add(newRule);
        return newRule;
    }

    @Override
    public IValidationRule updateRule(long id, String name, boolean activeStatus, ValidationAction action, List<String> mRIDs, Map<String, Object> properties) {
        IValidationRule rule =  doUpdateRule(getExistingRule(id), name, activeStatus, action,  mRIDs, properties);
        Save.UPDATE.validate(dataModel,rule);
        return rule;
    }

    @Override
    public IValidationRule cloneRule(IValidationRule iValidationRule){
        IValidationRule newRule = newRule(iValidationRule.getAction(), iValidationRule.getImplementation(), iValidationRule.getName());
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
            dataModel.touch(this);
        } else {
            throw new IllegalArgumentException("The rulset version" + this.getId() + " doesn't contain provided ruleId: " + rule.getId());
        }
    }

    @Override
    public String toString() {
        return doGetRules()
                .map(ValidationRule::toString)
                .collect(Collectors.joining("\n", null, "\n"));
    }

    private DataMapper<IValidationRuleSetVersion> validationRuleSetVersionFactory() {
        return dataModel.mapper(IValidationRuleSetVersion.class);
    }

    @Override
    public List<ValidationRule> getRules(Collection<? extends ReadingType> readingTypes) {
        return getRules()
                .stream()
                .filter(rule -> readingTypes.stream().anyMatch(rule::appliesTo))
                .collect(Collectors.toList());
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
