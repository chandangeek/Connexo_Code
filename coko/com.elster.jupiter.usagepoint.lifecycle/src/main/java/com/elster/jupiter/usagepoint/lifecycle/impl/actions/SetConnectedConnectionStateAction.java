package com.elster.jupiter.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.ExecutableMicroActionException;
import com.elster.jupiter.usagepoint.lifecycle.impl.MicroCategory;

import java.time.Instant;
import java.util.Map;

public class SetConnectedConnectionStateAction extends TranslatableAction {

    @Override
    public String getCategory() {
        return MicroCategory.CONNECTION_STATE.name();
    }

    @Override
    public void execute(UsagePoint usagePoint, Instant transitionTime, Map<String, Object> properties) throws ExecutableMicroActionException {

    }
}
