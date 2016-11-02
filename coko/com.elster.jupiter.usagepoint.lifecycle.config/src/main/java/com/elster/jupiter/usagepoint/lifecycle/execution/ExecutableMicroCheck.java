package com.elster.jupiter.usagepoint.lifecycle.execution;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroCheck;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Map;

@ConsumerType
public interface ExecutableMicroCheck extends MicroCheck {

    default void execute(UsagePoint usagePoint, Map<String, Object> properties, Instant transitionTime) {
    }
}
