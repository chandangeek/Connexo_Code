package com.elster.jupiter.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

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
