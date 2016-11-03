package com.elster.jupiter.usagepoint.lifecycle;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.usagepoint.lifecycle.config.MicroAction;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.Map;

@ConsumerType
public interface ExecutableMicroAction extends MicroAction {

    default void execute(UsagePoint usagePoint, Map<String, Object> properties, Instant transitionTime) {
    }
}
