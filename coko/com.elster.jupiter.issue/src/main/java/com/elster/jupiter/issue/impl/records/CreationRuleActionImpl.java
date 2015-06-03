package com.elster.jupiter.issue.impl.records;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.validator.HasValidProperties;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionPhase;
import com.elster.jupiter.issue.share.entity.CreationRuleActionProperty;
import com.elster.jupiter.issue.share.entity.IssueActionType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;

@HasValidProperties(groups = {Save.Create.class, Save.Update.class})
public class CreationRuleActionImpl implements CreationRuleAction {

    @SuppressWarnings("unused")
    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private CreationRuleActionPhase phase;
    @IsPresent
    private Reference<CreationRule> rule = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueActionType> action = ValueReference.absent();
    @Valid
    private List<CreationRuleActionProperty> properties = new ArrayList<>();

    // Audit fields
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    
    private final DataModel dataModel;
    
    @Inject
    public CreationRuleActionImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public IssueActionType getAction() {
        return action.get();
    }

    @Override
    public CreationRuleActionPhase getPhase() {
        return phase;
    }

    @Override
    public List<CreationRuleActionProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public CreationRule getRule() {
        return rule.get();
    }

    @Override
    public String getDisplayName(String propertyName) {
        return getAction().createIssueAction().map(action -> action.getDisplayName(propertyName)).orElse(null);
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        if (action.isPresent()) {
            Optional<IssueAction> issueAction = getAction().createIssueAction();
            if(issueAction.isPresent()) {
                return issueAction.get().getPropertySpecs();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public PropertySpec getPropertySpec(String propertyName) {
        return getPropertySpecs().stream()
                .filter(p -> propertyName.equals(p.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<String, Object> getProps() {
        return this.properties.stream().collect(Collectors.toMap(CreationRuleActionProperty::getName, CreationRuleActionProperty::getValue));
    }

    void setAction(IssueActionType type) {
        this.action.set(type);
    }

    void setPhase(CreationRuleActionPhase phase) {
        this.phase = phase;
    }

    CreationRuleActionProperty addProperty(String name, Object value) {
        CreationRuleActionProperty newProperty = dataModel.getInstance(CreationRuleActionPropertyImpl.class).init(this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    void setRule(CreationRule rule) {
        this.rule.set(rule);
    }
}
