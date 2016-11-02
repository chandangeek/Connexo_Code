package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.checks;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroCheck;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.Map;

public interface ServerMicroCheck extends MicroCheck {

    default void execute(UsagePoint usagePoint, Map<String, Object> properties, Instant transitionTime) {
    }
}
