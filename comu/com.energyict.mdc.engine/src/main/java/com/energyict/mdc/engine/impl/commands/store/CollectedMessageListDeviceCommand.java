package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.CollectedDeviceData;
import com.energyict.mdc.engine.impl.meterdata.DeviceProtocolMessageList;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;

import java.util.List;

/**
 * Provides functionality to store {@link DeviceMessage DeviceMessage}
 * results in the database.
 * <p/>
 * Copyrights EnergyICT
 * Date: 21/03/13
 * Time: 16:40
 */
public class CollectedMessageListDeviceCommand extends DeviceCommandImpl {

    private final DeviceProtocolMessageList deviceProtocolMessageList;
    private final List<OfflineDeviceMessage> allDeviceMessages;
    private final MeterDataStoreCommand meterDataStoreCommand;

    public CollectedMessageListDeviceCommand(DeviceProtocolMessageList deviceProtocolMessageList, List<OfflineDeviceMessage> allDeviceMessages, MeterDataStoreCommand meterDataStoreCommand) {
        super();
        this.deviceProtocolMessageList = deviceProtocolMessageList;
        this.allDeviceMessages = allDeviceMessages;
        this.meterDataStoreCommand = meterDataStoreCommand;
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        for (OfflineDeviceMessage offlineDeviceMessage : allDeviceMessages) {
            boolean notFound = true;
            final MessageIdentifier offlineDeviceMessageIdentifier = offlineDeviceMessage.getIdentifier();
            for (CollectedMessage collectedMessage : this.deviceProtocolMessageList.getCollectedMessages()) {
                if(collectedMessage.getMessageIdentifier().equals(offlineDeviceMessageIdentifier)){
                    comServerDAO.updateDeviceMessageInformation(collectedMessage.getMessageIdentifier(), collectedMessage.getNewDeviceMessageStatus(), collectedMessage.getDeviceProtocolInformation());
                    notFound = false;
                }

                if (CollectedDeviceData.class.isAssignableFrom(collectedMessage.getClass())) {
                    DeviceCommand deviceCommand = ((CollectedDeviceData) collectedMessage).toDeviceCommand(getIssueService(), meterDataStoreCommand);
                    deviceCommand.execute(comServerDAO);
                }
            }
            // messages that are not processed need to inform the user why they are not processed during a comTaskExecution
            if (notFound) {
                comServerDAO.updateDeviceMessageInformation(offlineDeviceMessageIdentifier, offlineDeviceMessage.getDeviceMessageStatus(), CollectedMessageList.REASON_FOR_PENDING_STATE);
            }

        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            for (CollectedMessage collectedMessage : deviceProtocolMessageList.getCollectedMessages()) {
                builder.addListProperty("messageIdentifier").append(collectedMessage.getMessageIdentifier()).next()
                        .append("message status: ").append(collectedMessage.getNewDeviceMessageStatus()).next()
                        .append("protocolInfo: ").append(collectedMessage.getDeviceProtocolInformation()).next();
            }
        } else if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            for (CollectedMessage collectedMessage : deviceProtocolMessageList.getCollectedMessages()) {
                builder.addListProperty("messageIdentifier").append(collectedMessage.getMessageIdentifier()).next()
                        .append("message status: ").append(collectedMessage.getNewDeviceMessageStatus()).next();
            }
        }
    }
}
