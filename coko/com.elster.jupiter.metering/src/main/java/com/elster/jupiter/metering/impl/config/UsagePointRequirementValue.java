/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.UsagePointRequirement;
import com.elster.jupiter.metering.impl.PrivateMessageSeeds;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;

public class UsagePointRequirementValue {
    public enum Fields {
        USAGE_POINT_REQUIREMENT("usagePointRequirement"),
        VALUE("value"),
        POSITION("position"),;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    private Reference<UsagePointRequirement> usagePointRequirement = ValueReference.absent();
    @NotNull(message = "{" + PrivateMessageSeeds.Constants.REQUIRED + "}")
    @Size(min = 1, max = Table.SHORT_DESCRIPTION_LENGTH)
    private String value;

    @SuppressWarnings("unused")
    private int position;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    UsagePointRequirementValue init(UsagePointRequirement usagePointRequirement, String value) {
        this.usagePointRequirement.set(usagePointRequirement);
        this.value = value;
        return this;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsagePointRequirementValue that = (UsagePointRequirementValue) o;
        return position == that.position && usagePointRequirement.equals(that.usagePointRequirement);
    }

    @Override
    public int hashCode() {
        int result = usagePointRequirement.hashCode();
        result = 31 * result + position;
        return result;
    }
}
