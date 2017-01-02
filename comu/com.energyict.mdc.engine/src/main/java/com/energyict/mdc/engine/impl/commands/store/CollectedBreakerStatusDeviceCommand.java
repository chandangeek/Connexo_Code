package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedBreakerStatusEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides functionality to update the BreakerStatus of a Device
 */
public class CollectedBreakerStatusDeviceCommand extends DeviceCommandImpl<CollectedBreakerStatusEvent> {

    public final static String DESCRIPTION_TITLE = "Collected breaker status";

    private final CollectedBreakerStatus collectedBreakerStatus;
    private final ComTaskExecution comTaskExecution;

    public CollectedBreakerStatusDeviceCommand(ServiceProvider serviceProvider, CollectedBreakerStatus collectedBreakerStatus, ComTaskExecution comTaskExecution) {
        super(comTaskExecution, serviceProvider);
        this.collectedBreakerStatus = collectedBreakerStatus;
        this.comTaskExecution = comTaskExecution;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.updateBreakerStatus(collectedBreakerStatus);
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        collectedBreakerStatus.getBreakerStatus().ifPresent(addBuilderProperty(builder, "breaker status"));
    }

    private Consumer<BreakerStatus> addBuilderProperty(DescriptionBuilder builder, String propertyName) {
        return property -> builder.addProperty(propertyName).append(property.toString().toLowerCase());
    }

    protected Optional<CollectedBreakerStatusEvent> newEvent(List<Issue> issues) {
        CollectedBreakerStatusEvent event = new CollectedBreakerStatusEvent(new ComServerEventServiceProvider(), collectedBreakerStatus);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }
}