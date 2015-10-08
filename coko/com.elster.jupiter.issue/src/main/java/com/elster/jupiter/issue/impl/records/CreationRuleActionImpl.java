package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
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
import com.elster.jupiter.properties.HasValidProperties;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@HasValidProperties(requiredPropertyMissingMessage = "{" + MessageSeeds.Keys.PROPERTY_MISSING + "}",
                    propertyNotInSpecMessage = "{" + MessageSeeds.Keys.PROPERTY_NOT_IN_PROPERTYSPECS + "}")
public class CreationRuleActionImpl extends EntityImpl implements CreationRuleAction {

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private CreationRuleActionPhase phase;
    @IsPresent
    private Reference<CreationRule> rule = ValueReference.absent();
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private Reference<IssueActionType> type = ValueReference.absent();
    @Valid
    private List<CreationRuleActionProperty> properties = new ArrayList<>();

    @Inject
    public CreationRuleActionImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public IssueActionType getAction() {
        return type.get();
    }

    @Override
    public CreationRuleActionPhase getPhase() {
        return phase;
    }

    @Override
    public List<CreationRuleActionProperty> getCreationRuleActionProperties() {
        return Collections.unmodifiableList(properties);
    }

    @Override
    public CreationRule getRule() {
        return rule.get();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        if (type.isPresent()) {
            Optional<IssueAction> issueAction = getAction().createIssueAction();
            if(issueAction.isPresent()) {
                return issueAction.get().getPropertySpecs();
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.properties.stream().collect(Collectors.toMap(CreationRuleActionProperty::getName, CreationRuleActionProperty::getValue));
    }
    
    @Override
    public void validate() {
        Save.CREATE.validate(getDataModel(), this);
    }

    void setAction(IssueActionType type) {
        this.type.set(type);
    }

    void setPhase(CreationRuleActionPhase phase) {
        this.phase = phase;
    }

    CreationRuleActionProperty addProperty(String name, Object value) {
        CreationRuleActionProperty newProperty = getDataModel().getInstance(CreationRuleActionPropertyImpl.class).init(this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    void setRule(CreationRule rule) {
        this.rule.set(rule);
    }
}
