package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedCalendarEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides functionality to update the active and passive calendar(s) of a Device.
 */
public class CollectedCalendarDeviceCommand extends DeviceCommandImpl<CollectedCalendarEvent> {

    public static final String DESCRIPTION_TITLE = "Collected calendar";

    private final CollectedCalendar collectedCalendar;

    public CollectedCalendarDeviceCommand(ServiceProvider serviceProvider, CollectedCalendar collectedCalendar, ComTaskExecution comTaskExecution) {
        super(comTaskExecution, serviceProvider);
        this.collectedCalendar = collectedCalendar;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        if (!this.collectedCalendar.isEmpty()) {
            comServerDAO.updateCalendars(this.collectedCalendar);
        }
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        this.collectedCalendar.getActiveCalendar().ifPresent(addBuilderProperty(builder, "active calendar"));
        this.collectedCalendar.getPassiveCalendar().ifPresent(addBuilderProperty(builder, "passive calendar"));
    }

    private Consumer<String> addBuilderProperty(DescriptionBuilder builder, String propertyName) {
        return calendarName -> builder.addProperty(propertyName).append(calendarName);
    }

    protected Optional<CollectedCalendarEvent> newEvent(List<Issue> issues) {
        if (!this.collectedCalendar.isEmpty()) {
            CollectedCalendarEvent event = new CollectedCalendarEvent(new ComServerEventServiceProvider(), this.collectedCalendar);
            event.addIssues(issues);
            return Optional.of(event);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}