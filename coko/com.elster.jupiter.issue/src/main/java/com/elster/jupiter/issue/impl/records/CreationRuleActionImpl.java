package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.Checks.is;

public class CreationRuleActionImpl extends EntityImpl implements CreationRuleAction{

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    private CreationRuleActionPhase phase;
    private Reference<CreationRule> rule = ValueReference.absent();
    private Reference<IssueActionType> type = ValueReference.absent();
    private List<ActionParameter> parameters = new ArrayList<>();

    @Inject
    public CreationRuleActionImpl(DataModel dataModel) {
        super(dataModel);
    }

    public CreationRuleActionPhase getPhase() {
        return phase;
    }

    public void setPhase(CreationRuleActionPhase phase) {
        this.phase = phase;
    }

    public CreationRule getRule() {
        return rule.orNull();
    }

    public void setRule(CreationRule rule) {
        this.rule.set(rule);
    }

    public IssueActionType getType() {
        return type.orNull();
    }

    public void setType(IssueActionType type) {
        this.type.set(type);
    }

    public List<ActionParameter> getParameters() {
        return parameters;
    }

    public void addParameter(String key, String value){
        if (!is(key).emptyOrOnlyWhiteSpace() && !is(value).emptyOrOnlyWhiteSpace()) {
            ActionParameterImpl parameter = getDataModel().getInstance(ActionParameterImpl.class);
            parameter.setKey(key);
            parameter.setValue(value);
            parameter.setAction(this);
            getParameters().add(parameter);
        }
    }
}
