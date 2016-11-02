package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions;

import com.elster.jupiter.mdm.usagepoint.lifecycle.MicroAction;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.Map;

public interface ServerMicroAction extends MicroAction {

    default void execute(UsagePoint usagePoint, Map<String, Object> properties, Instant transitionTime) {
    }
}
