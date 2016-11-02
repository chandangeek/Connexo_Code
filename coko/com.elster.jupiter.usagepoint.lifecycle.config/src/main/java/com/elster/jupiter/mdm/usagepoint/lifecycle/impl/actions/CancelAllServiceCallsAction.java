package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCategory;

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
