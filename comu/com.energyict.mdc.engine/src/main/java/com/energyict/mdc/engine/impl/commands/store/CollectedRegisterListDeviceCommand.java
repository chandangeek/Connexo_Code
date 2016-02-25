package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedRegisterListEvent;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import com.elster.jupiter.metering.readings.Reading;

import java.util.*;

/**
 * Provides functionality to store {@link com.energyict.mdc.protocol.api.device.BaseRegister} data into the system.
 *
 * @author sva
 * @since 21/01/13 - 9:16
 */

public class CollectedRegisterListDeviceCommand extends DeviceCommandImpl<CollectedRegisterListEvent> {

    public final static String DESCRIPTION_TITLE = "Collected register data";

    private final CollectedRegisterList collectedRegisterList;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedRegisterListDeviceCommand(CollectedRegisterList collectedRegisterList, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.collectedRegisterList = collectedRegisterList;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        PreStoreRegisters preStoreRegisters = new PreStoreRegisters(this.getMdcReadingTypeUtilService(), comServerDAO);
        Map<DeviceIdentifier, List<Reading>> readings = preStoreRegisters.preStore(collectedRegisterList);
        this.addIssuesForUnknownRegisters(preStoreRegisters.getUnknownRegisters());
        readings.forEach(this.meterDataStoreCommand::addReadings);
    }

    private void addIssuesForUnknownRegisters(List<RegisterIdentifier> unknownRegisters) {
        unknownRegisters.forEach(this::addIssuesForUnknownRegister);
    }

    private void addIssuesForUnknownRegister(RegisterIdentifier registerIdentifier) {
        this.addIssue(
                CompletionCode.ConfigurationWarning,
                this.getIssueService().newWarning(
                        this,
                        MessageSeeds.UNKNOWN_DEVICE_REGISTER.getKey(),
                        registerIdentifier));
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.collectedRegisterList.getDeviceIdentifier());
            builder.addProperty("nr of collected registers").append(this.collectedRegisterList.getCollectedRegisters().size());
        }
    }

    protected Optional<CollectedRegisterListEvent> newEvent(Issue issue) {
        CollectedRegisterListEvent event  =  new CollectedRegisterListEvent(new ComServerEventServiceProvider(), collectedRegisterList);
        if (issue != null){
            event.setIssue(issue);
        }
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}
