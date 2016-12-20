package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.CollectedMessageListEvent;
import com.energyict.mdc.engine.impl.meterdata.CollectedDeviceData;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.tasks.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Provides functionality to store {@link DeviceMessage DeviceMessage}
 * results in the database.
 * <p>
 * Copyrights EnergyICT
 * Date: 21/03/13
 * Time: 16:40
 */
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
            List<CollectedMessage> messagesToExecute = this.deviceProtocolMessageList.getCollectedMessages(offlineDeviceMessage.getIdentifier());
            if (messagesToExecute.isEmpty()) {
                comServerDAO.updateDeviceMessageInformation(offlineDeviceMessage.getIdentifier(), offlineDeviceMessage.getDeviceMessageStatus(), null, CollectedMessageList.REASON_FOR_PENDING_STATE);
                continue;
            }

            // execute the 'executable' ones
            messagesToExecute.stream().filter(x -> x instanceof ServerCollectedData).forEach(y -> executeMessage(comServerDAO, y));
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
        comServerDAO.updateDeviceMessageInformation(collectedMessage.getMessageIdentifier(),
                collectedMessage.getNewDeviceMessageStatus(),
                collectedMessage.getSentDate(),
                collectedMessage.getDeviceProtocolInformation());
        ((CollectedDeviceData) collectedMessage).toDeviceCommand(this.meterDataStoreCommand, this.getServiceProvider()).execute(comServerDAO);
    }

}