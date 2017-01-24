package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.EventType;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.NoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedNoLogBooksForDeviceEvent;
import com.energyict.mdc.engine.impl.meterdata.NoLogBooksForDevice;
import com.energyict.mdc.upl.issue.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will create an NoLogBooksForDeviceEvent.
 *
 * @author sva
 * @since 14/12/12 - 9:15
 */

public class CreateNoLogBooksForDeviceEvent extends DeviceCommandImpl<CollectedNoLogBooksForDeviceEvent> {

    public static final String DESCRIPTION_TITLE = "No logbooks for device event";

    private final NoLogBooksForDevice collectedDeviceData;

    public CreateNoLogBooksForDeviceEvent(NoLogBooksForDevice collectedDeviceData, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.collectedDeviceData = collectedDeviceData;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.signalEvent(EventType.NO_LOGBOOKS_FOR_DEVICE.topic(), new NoLogBooksForDeviceEvent(this.collectedDeviceData.getDeviceIdentifier()));
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.collectedDeviceData.getDeviceIdentifier());
        }
    }

    protected Optional<CollectedNoLogBooksForDeviceEvent> newEvent(List<Issue> issues) {
        CollectedNoLogBooksForDeviceEvent event  =  new CollectedNoLogBooksForDeviceEvent(new ComServerEventServiceProvider(), collectedDeviceData);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}