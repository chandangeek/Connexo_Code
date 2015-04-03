package com.elster.jupiter.validation.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.validation.MessageSeeds;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

//@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_VALIDATION_RULE_SET_VERSION + "}")
//@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
public class ValidationRuleSetVersionImpl implements IValidationRuleSetVersion {
    private long id;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    @Size(min = 1, max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    private String name;

    private Instant startDate;

    private Reference<ValidationRuleSet> ruleSet = ValueReference.absent();
    private List<IValidationRule> rules = new ArrayList<>();
    private List<IValidationRule> rulesToSave = new ArrayList<>();

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Instant getStartDate() {
        return startDate;
    }

    public Reference<ValidationRuleSet> getRuleSet() {
        return ruleSet;
    }

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
        eventService.postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
        eventService.postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
    }

    @Override
    public void delete() {
        doGetRules().forEach(rule -> rule. delete());
        validationRuleSetFactory().update(this);
        eventService.postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
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

    private DataMapper<IValidationRuleSetVersion> validationRuleSetFactory() {
        return dataModel.mapper(IValidationRuleSetVersion.class);
    }

    @Override
    public List<IValidationRule> getRules() {
        return doGetRules()
                .sorted(Comparator.comparing(rule -> rule.getName().toUpperCase()))
                .collect(Collectors.toList());
    }

    public List<IValidationRule> getRules(int start, int limit) {
        return Collections.unmodifiableList(
                getRuleQuery().select(
                        Where.where("ruleSet").isEqualTo(this),
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

}
