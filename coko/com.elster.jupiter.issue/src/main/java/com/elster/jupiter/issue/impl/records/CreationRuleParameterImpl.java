package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleParameter;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreationRuleParameterImpl extends EntityImpl implements CreationRuleParameter {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 256, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_256 + "}")
    private String key;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 1024, message = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_1024 + "}")
    private String value;
    private Reference<CreationRule> rule = ValueReference.absent();

    @Inject
    public CreationRuleParameterImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public CreationRule getRule() {
        return rule.orNull();
    }

    @Override
    public void setRule(CreationRule rule) {
        this.rule.set(rule);
    }
}
