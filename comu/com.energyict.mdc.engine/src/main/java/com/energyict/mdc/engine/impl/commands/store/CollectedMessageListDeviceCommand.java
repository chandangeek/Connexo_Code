/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedMessageListEvent;
import com.energyict.mdc.engine.impl.meterdata.CollectedDeviceData;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageWithCollectedRegisterData;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.tasks.CompletionCode;
import com.energyict.obis.ObisCode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CollectedMessageListDeviceCommand extends DeviceCommandImpl<CollectedMessageListEvent> {

    public static final String DESCRIPTION_TITLE = "Collected message data";

    private final DeviceProtocolMessageList deviceProtocolMessageList;
    private final List<OfflineDeviceMessage> allDeviceMessages;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedMessageListDeviceCommand(DeviceProtocolMessageList deviceProtocolMessageList, List<OfflineDeviceMessage> allDeviceMessages, ComTaskExecution comTaskExecution, MeterDataStoreCommand meterDataStoreCommand, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceProtocolMessageList = deviceProtocolMessageList;
        this.allDeviceMessages = allDeviceMessages;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void logExecutionWith(ExecutionLogger logger) {
        super.logExecutionWith(logger);
        if (deviceProtocolMessageList != null) {
            deviceProtocolMessageList.getCollectedMessages().stream()
                    .filter(x -> x instanceof CollectedDeviceData)
                    .map(CollectedDeviceData.class::cast)
                    .map(y -> y.toDeviceCommand(this.meterDataStoreCommand, this.getServiceProvider()))
                    .forEach(x -> x.logExecutionWith(logger));
        }
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        for (OfflineDeviceMessage offlineDeviceMessage : allDeviceMessages) {
            List<CollectedMessage> messagesToExecute = this.deviceProtocolMessageList.getCollectedMessages(offlineDeviceMessage.getMessageIdentifier());
            if (messagesToExecute.isEmpty()) {
                comServerDAO.updateDeviceMessageInformation(offlineDeviceMessage.getMessageIdentifier(), offlineDeviceMessage.getDeviceMessageStatus(), null, CollectedMessageList.REASON_FOR_PENDING_STATE);
                continue;
            }

            // execute the 'executable' ones
            List<CollectedMessage> executedMessages = messagesToExecute.stream()
                    .filter(x -> x instanceof ServerCollectedData)
                    .peek(y -> executeMessage(comServerDAO, y))
                    .collect(Collectors.toList());

            //process csr
            if (offlineDeviceMessage.getDeviceMessageAttributes().stream().anyMatch(e -> e.getDeviceMessageId() == 7048)) {
                executedMessages.stream()
                        .filter(e -> e.getNewDeviceMessageStatus().equals(DeviceMessageStatus.CONFIRMED) && !Checks.is(e.getDeviceProtocolInformation()).emptyOrOnlyWhiteSpace())
                        .forEach(e -> comServerDAO.updateDeviceCSR(offlineDeviceMessage.getDeviceIdentifier(), offlineDeviceMessage.getDeviceMessageAttributes()
                                .get(0)
                                .getValue(), e.getDeviceProtocolInformation()));
            }
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            for (CollectedMessage collectedMessage : deviceProtocolMessageList.getCollectedMessages()) {
                builder.addListProperty("messageIdentifier").append(collectedMessage.getMessageIdentifier()).next()
                        .append("message status: ").append(collectedMessage.getNewDeviceMessageStatus()).next()
                        .append("sent date: ").append(collectedMessage.getSentDate()).next()
                        .append("protocolInfo: ").append(collectedMessage.getDeviceProtocolInformation()).next();
            }
        } else if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            for (CollectedMessage collectedMessage : deviceProtocolMessageList.getCollectedMessages()) {
                builder.addListProperty("messageIdentifier").append(collectedMessage.getMessageIdentifier()).next()
                        .append("message status: ").append(collectedMessage.getNewDeviceMessageStatus()).next();
            }
        }
    }

    protected Optional<CollectedMessageListEvent> newEvent(List<Issue> issues) {
        CollectedMessageListEvent event = new CollectedMessageListEvent(new ComServerEventServiceProvider(), deviceProtocolMessageList);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

    private void executeMessage(ComServerDAO comServerDAO, CollectedMessage collectedMessage) {
        try {
            comServerDAO.updateDeviceMessageInformation(collectedMessage.getMessageIdentifier(),
                    collectedMessage.getNewDeviceMessageStatus(),
                    collectedMessage.getSentDate(),
                    collectedMessage.getDeviceProtocolInformation());

           Optional<CollectedRegister> collectedRegisterBreakerStatus = getCollectedRegisterBreakerStatus(collectedMessage);
            collectedRegisterBreakerStatus.ifPresent(collectedRegister -> comServerDAO.storeBreakerStatus(collectedRegisterBreakerStatus.get(), collectedMessage));

            ((CollectedDeviceData) collectedMessage).toDeviceCommand(this.meterDataStoreCommand, this.getServiceProvider()).execute(comServerDAO);
        } catch (Throwable t) {
            addIssueToExecutionLogger(CompletionCode.UnexpectedError,
                    getIssueService().newProblem(this, MessageSeeds.COLLECTED_DATA_ISSUE, collectedMessage.getMessageIdentifier(), t.getLocalizedMessage()));
        }
    }

    private  Optional<CollectedRegister> getCollectedRegisterBreakerStatus(CollectedMessage collectedMessage) {
        return ((DeviceProtocolMessageWithCollectedRegisterData) collectedMessage).getCollectedRegisters().stream()
                .filter(register -> register.getRegisterIdentifier().getRegisterObisCode().equals(ObisCode.fromString("0.0.96.3.10.255")))
                .findFirst();
    }
}