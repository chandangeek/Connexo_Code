/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.CreationRuleProperty;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;

public class CreationRulePropertyImpl extends AbstractPropertyImpl implements CreationRuleProperty {

    @IsPresent
    private Reference<CreationRule> rule = ValueReference.absent();

    CreationRulePropertyImpl init(CreationRuleImpl rule, String name, Object value) {
        this.rule.set(rule);
        super.init(name, value);
        return this;
    }

    @Override
    public CreationRule getRule() {
        return rule.get();
    }

    protected PropertySpec getPropertySpec() {
        return rule.get().getPropertySpec(getName()).get();
    }

}