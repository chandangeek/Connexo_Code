package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.util.Objects;

public class EstimationRuleInfo {

    public long id;
    public String displayName;
    public long ruleSetId;

    public EstimationRuleInfo() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return id == ((EstimationRuleInfo) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
