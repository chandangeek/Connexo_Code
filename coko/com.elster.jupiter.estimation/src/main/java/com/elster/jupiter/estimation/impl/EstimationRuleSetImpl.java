package com.elster.jupiter.estimation.impl;

import static com.elster.jupiter.util.conditions.Where.where;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationRule;
import com.elster.jupiter.estimation.EstimationRuleProperties;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.impl.MessageSeeds.Constants;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.DUPLICATE_ESTIMATION_RULE_SET + "}")
class EstimationRuleSetImpl implements IEstimationRuleSet {

    static final String OBSOLETE_TIME_FIELD = "obsoleteTime";

    private long id;
    private String mRID;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;
    @Size(min = 0, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String aliasName;
    @Size(min = 0, max = Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + Constants.FIELD_SIZE_BETWEEN_1_AND_4000 + "}")
    private String description;
    private Instant obsoleteTime;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    @Valid
    private List<IEstimationRule> rules = new ArrayList<>();

    private List<IEstimationRule> rulesToSave = new ArrayList<>();

    private final EventService eventService;
    private final DataModel dataModel;
    private final Provider<EstimationRuleImpl> validationRuleProvider;

    @Inject
    EstimationRuleSetImpl(DataModel dataModel, EventService eventService, Provider<EstimationRuleImpl> validationRuleProvider) {
        // for persistence
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.validationRuleProvider = validationRuleProvider;
    }

    EstimationRuleSetImpl init(String name) {
        return init(name,null);
    }

    EstimationRuleSetImpl init(String name, String description) {
        this.name = Checks.is(name).emptyOrOnlyWhiteSpace() ? null : name.trim();
        this.description = description;
        return this;
    }

    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setMRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long getVersion() {
        return version;
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
        if (!(o instanceof EstimationRuleSet)) {
            return false;
        }

        EstimationRuleSet validationRuleSet = (EstimationRuleSet) o;

        return id == validationRuleSet.getId();
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
        doGetRules().forEach( rule -> rule.save());
        eventService.postEvent(EventType.ESTIMATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.ESTIMATIONRULESET_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        this.setObsoleteTime(Instant.now()); // mark obsolete
        doGetRules().forEach(rule -> rule.delete());
        validationRuleSetFactory().update(this);
        eventService.postEvent(EventType.ESTIMATIONRULESET_DELETED.topic(), this);
    }

    @Override
    public List<IEstimationRule> getRules() {
        return doGetRules()
                .collect(Collectors.toList());
    }

    private void addNewRules() {
        rulesToSave.forEach( newRule -> {
            Save.CREATE.validate(dataModel, newRule);
            rules.add(newRule);
        });
        rulesToSave.clear();
    }

    private Stream<IEstimationRule> doGetRules() {
        return rules.stream().filter(rule -> !rule.isObsolete());
    }

    public List<IEstimationRule> getRules(int start, int limit) {
        return Collections.unmodifiableList(
                getRuleQuery().select(
                        Where.where("ruleSet").isEqualTo(this),
                        new Order[]{Order.ascending("position")},
                        false,
                        new String[]{},
                        start + 1,
                        start + limit));
    }

    private QueryExecutor<IEstimationRule> getRuleQuery() {
        QueryExecutor<IEstimationRule> ruleQuery = dataModel.query(IEstimationRule.class, EstimationRuleProperties.class);
        ruleQuery.setRestriction(where("obsoleteTime").isNull());
        return ruleQuery;
    }

    @Override
    public IEstimationRule addRule(String implementation, String name) {
        EstimationRuleImpl newRule = validationRuleProvider.get().init(this, implementation, name);
        rulesToSave.add(newRule);
        return newRule;
    }

    @Override
    public IEstimationRule updateRule(long id, String name, boolean activeStatus, List<String> mRIDs, Map<String, Object> properties) {
        IEstimationRule rule = getExistingRule(id);
        return doUpdateRule(rule, name, activeStatus, mRIDs, properties);
    }

    @Override
    public void reorderRules(KPermutation kpermutation) {
        List<IEstimationRule> filteredRules = getRules();
        if (!kpermutation.isPermutation(filteredRules)) {
            throw new IllegalArgumentException();
        }
        List<IEstimationRule> target = kpermutation.perform(filteredRules);
        target.addAll(obsoleteRules());
        dataModel.reorder(rules, target);
    }

    private List<IEstimationRule> obsoleteRules() {
        return rules.stream()
                .filter(IEstimationRule::isObsolete)
                .collect(Collectors.toList());
    }

    private IEstimationRule doUpdateRule(IEstimationRule rule, String name,  boolean activeStatus, List<String> mRIDs, Map<String, Object> properties) {
        rule.rename(name);

        if (activeStatus != rule.isActive()) {
            rule.toggleActivation();
        }
        updateReadingTypes(rule, mRIDs);
        rule.setProperties(properties);
        rule.save();

        return rule;
    }

    private void updateReadingTypes(IEstimationRule rule, List<String> mRIDs) {
        rule.clearReadingTypes();
        mRIDs.stream().forEach(rule::addReadingType);
    }

    private IEstimationRule getExistingRule(long id) {
        return doGetRules()
                .filter(rule -> rule.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The ruleset " + this.getId() + " doesn't contain provided ruleId: " + id));
    }

    @Override
    public void deleteRule(EstimationRule rule) {
        IEstimationRule iRule = (IEstimationRule) rule;
        if (doGetRules().anyMatch( candidate -> candidate.equals(iRule))) {
            iRule.delete();
        } else {
            throw new IllegalArgumentException("The rulset " + this.getId() + " doesn't contain provided ruleId: " + rule.getId());
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getName());
        builder.append('\n');
        doGetRules().forEach(rule -> builder.append(rule.toString()).append('\n'));
        return builder.toString();
    }

    private DataMapper<IEstimationRuleSet> validationRuleSetFactory() {
        return dataModel.mapper(IEstimationRuleSet.class);
    }

    public List<IEstimationRule> getRules(Set<? extends ReadingType> readingTypes) {
        return getRules().stream()
                .filter(rule -> rule.getReadingTypes().stream().anyMatch(readingTypes::contains))
                .collect(Collectors.toList());
    }

    @Override
    public Instant getObsoleteDate() {
        return this.obsoleteTime;
    }

    private void setObsoleteTime(Instant obsoleteTime) {
        this.obsoleteTime = obsoleteTime;
    }

}
