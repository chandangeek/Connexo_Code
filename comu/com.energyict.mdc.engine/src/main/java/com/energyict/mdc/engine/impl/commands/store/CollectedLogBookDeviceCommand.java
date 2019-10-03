/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.device.data.InboundConnectionTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.history.CompletionCode;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedLogBookEvent;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to store {@link com.energyict.mdc.upl.meterdata.LogBook} data into the system.
 *
 * @author sva
 * @since 10/12/12 - 11:13
 */
public class CollectedLogBookDeviceCommand extends DeviceCommandImpl<CollectedLogBookEvent> {

    public static final String DESCRIPTION_TITLE = "Collected logbook data";

    private final CollectedLogBook deviceLogBook;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedLogBookDeviceCommand(CollectedLogBook deviceLogBook, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand) {
        super(comTaskExecution, meterDataStoreCommand.getServiceProvider());
        this.deviceLogBook = deviceLogBook;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        if (comServerDAO.findOfflineLogBook(deviceLogBook.getLogBookIdentifier()) != null) {
            comServerDAO.storeLogBookData(deviceLogBook.getLogBookIdentifier(), deviceLogBook, this.getClock().instant());
//            if (!isAwareOfPushedEvents()) {
//                comServerDAO.updateLogBookLastReadingFromTask(deviceLogBook.getLogBookIdentifier(), getComTaskExecution().getId());
//            } else if(isOutboundConnection()){ //do not update for inbound, EISERVERSG-4265
//                comServerDAO.updateLogBookLastReadingFromTask(deviceLogBook.getLogBookIdentifier(), getComTaskExecution().getId());
//            }
        } else {
            this.addIssue(
                    CompletionCode.ConfigurationWarning,
                    this.getIssueService().newWarning(
                            this,
                            MessageSeeds.UNKNOWN_DEVICE_LOG_BOOK,
                            this.deviceLogBook.getLogBookIdentifier()));
        }
    }

    private boolean isAwareOfPushedEvents() {
        return deviceLogBook.isAwareOfPushedEvents();
    }

    private boolean isOutboundConnection() {
        try{
            Optional<ConnectionTask<?, ?>> connectionTask = getComTaskExecution().getConnectionTask();
            if (connectionTask.isPresent()) {
                if (connectionTask.get() instanceof InboundConnectionTask) {
                    return false;
                }
            }
        } catch (Exception ex){
            return true;
        }
        return true;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("logbook").append(this.deviceLogBook.getLogBookIdentifier());
            builder.addProperty("nr of events").append(this.deviceLogBook.getCollectedMeterEvents().size());
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    protected Optional<CollectedLogBookEvent> newEvent(List<Issue> issues) {
        CollectedLogBookEvent event = new CollectedLogBookEvent(new ComServerEventServiceProvider(), deviceLogBook);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}