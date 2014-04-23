package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionParameter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreationRuleActionParameterImpl extends EntityImpl implements CreationRuleActionParameter{

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 256, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
    private String key;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 1024, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_1024 + "}")
    private String value;
    private Reference<CreationRuleAction> action = ValueReference.absent();

    @Inject
    public CreationRuleActionParameterImpl(DataModel dataModel) {
        super(dataModel);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CreationRuleAction getAction() {
        return action.orNull();
    }

    public void setAction(CreationRuleAction action) {
        this.action.set(action);
    }
}
