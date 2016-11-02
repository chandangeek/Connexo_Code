package com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.impl;

import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointState;
import com.elster.jupiter.mdm.usagepoint.lifecycle.UsagePointTransition;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.actions.ServerMicroAction;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.checks.ServerMicroCheck;
import com.elster.jupiter.mdm.usagepoint.lifecycle.impl.execution.UsagePointLifeCycleExecutionService;
import com.elster.jupiter.metering.UsagePoint;

import org.osgi.service.component.annotations.Component;

import java.time.Instant;
import java.util.Map;

@Component(name = "UsagePointLifeCycleExecutionServiceImpl",
        service = {UsagePointLifeCycleExecutionService.class, ServerUsagePointLifeCycleExecutionService.class},
        immediate = true)
public class UsagePointLifeCycleExecutionServiceImpl implements ServerUsagePointLifeCycleExecutionService {
    @SuppressWarnings("unused") // OSGI
    public UsagePointLifeCycleExecutionServiceImpl() {
    }

    @Override
    public void triggerTransition(UsagePoint usagePoint, UsagePointTransition transition, Map<String, Object> properties, Instant transitionTime) {
        this.triggerMicroChecks(usagePoint, transition, properties, transitionTime);
        this.triggerMicroActions(usagePoint, transition, properties, transitionTime);

    }

    private void triggerMicroChecks(UsagePoint usagePoint, UsagePointTransition transition, Map<String, Object> properties, Instant transitionTime) {
        transition.getChecks().stream()
                .map(ServerMicroCheck.class::cast)
                .forEach(action -> action.execute(usagePoint, properties, transitionTime));
    }

    private void triggerMicroActions(UsagePoint usagePoint, UsagePointTransition transition, Map<String, Object> properties, Instant transitionTime) {
        transition.getActions().stream()
                .map(ServerMicroAction.class::cast)
                .forEach(action -> action.execute(usagePoint, properties, transitionTime));
    }

    public void performTransition(UsagePoint usagePoint, UsagePointState targetState, Instant transitionTime) {
        // TODO change state and save into db
    }
}
