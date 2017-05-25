/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.properties;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.data.impl.MessageSeeds;

import javax.validation.constraints.Size;
import java.util.Objects;

public class ValidationEstimationOverriddenPropertyImpl {

    public enum Fields {

        RULE("channelValidationEstimationRule"),
        PROPERTY_NAME("propertyName"),
        PROPERTY_VALUE("propertyValue");

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName() {
            return this.name;
        }
    }

    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    private Reference<ValidationEstimationRuleOverriddenPropertiesImpl> channelValidationEstimationRule = ValueReference.absent();

    @NotEmpty(message = "{" + MessageSeeds.Keys.FIELD_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String propertyName;

    @Size(max = Table.SHORT_DESCRIPTION_LENGTH, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String propertyValue;

    public ValidationEstimationOverriddenPropertyImpl init(ValidationEstimationRuleOverriddenPropertiesImpl rule, String propertyName, Object propertyValue) {
        this.channelValidationEstimationRule.set(rule);
        this.propertyName = propertyName;
        this.propertyValue = rule.getPropertySpec(propertyName).getValueFactory().toStringValue(propertyValue);
        return this;
    }

    public String getName() {
        return this.propertyName;
    }

    public Object getValue() {
        return this.channelValidationEstimationRule.get()
                .getPropertySpec(this.propertyName)
                .getValueFactory()
                .fromStringValue(this.propertyValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidationEstimationOverriddenPropertyImpl that = (ValidationEstimationOverriddenPropertyImpl) o;
        return Objects.equals(channelValidationEstimationRule, that.channelValidationEstimationRule) &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelValidationEstimationRule, propertyName);
    }
}
