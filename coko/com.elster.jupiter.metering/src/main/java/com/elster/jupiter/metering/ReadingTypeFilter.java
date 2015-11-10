package com.elster.jupiter.metering;

import com.elster.jupiter.util.conditions.Condition;

public class ReadingTypeFilter {
    private Condition condition;

    public ReadingTypeFilter(Condition condition) {
        this.condition = condition;
    }

    public Condition getCondition() {
        return condition;
    }
}
