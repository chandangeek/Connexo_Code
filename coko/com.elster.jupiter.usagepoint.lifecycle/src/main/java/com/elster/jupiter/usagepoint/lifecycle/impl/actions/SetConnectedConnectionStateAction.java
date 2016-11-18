package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import java.time.Instant;
import java.util.Map;

public class SetConnectedConnectionStateAction extends TranslatableAction {

    @Override
    public String getCategory() {
        return MicroCategory.CONNECTION_STATE.name();
    }

    @Override
    protected void doExecute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) {

    }
}
