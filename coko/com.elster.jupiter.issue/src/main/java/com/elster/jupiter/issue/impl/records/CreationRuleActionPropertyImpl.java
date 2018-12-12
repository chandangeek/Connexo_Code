/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.CreationRuleAction;
import com.elster.jupiter.issue.share.entity.CreationRuleActionProperty;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;

public class CreationRuleActionPropertyImpl extends AbstractPropertyImpl implements CreationRuleActionProperty {

    @IsPresent
    private Reference<CreationRuleAction> action = ValueReference.absent();

    CreationRuleActionPropertyImpl init(CreationRuleAction action, String name, Object value) {
        this.action.set(action);
        super.init(name, value);
        return this;
    }

    @Override
    public CreationRuleAction getAction() {
        return action.get();
    }

    protected PropertySpec getPropertySpec() {
        return action.get().getPropertySpec(getName()).get();
    }

}