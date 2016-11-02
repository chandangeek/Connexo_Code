package com.elster.jupiter.usagepoint.lifecycle.execution.impl.checks;

import com.elster.jupiter.usagepoint.lifecycle.execution.impl.MicroCategory;

public class AllDataValidCheck extends TranslatableCheck {
    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getCategory() {
        return MicroCategory.VALIDATION.name();
    }
}
