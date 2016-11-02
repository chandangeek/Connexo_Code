package com.elster.jupiter.usagepoint.lifecycle.execution.impl.actions;

import com.elster.jupiter.usagepoint.lifecycle.execution.impl.MicroCategory;

public class CancelAllServiceCallsAction extends TranslatableAction {
    @Override
    public String getKey() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getCategory() {
        return MicroCategory.SERVICE_CALLS.name();
    }
}
