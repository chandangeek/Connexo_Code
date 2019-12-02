package com.energyict.mdc.engine.impl.core.offline.identifiers;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 28/10/2014 - 16:40
 */
public class MobileDeviceMessageFactory {

    public static OfflineDeviceMessage findOfflineDeviceMessage(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, MessageIdentifier messageIdentifier) {
        try {
            switch (messageIdentifier.forIntrospection().getTypeName()) {
                case "DatabaseId":
                    long dataBaseId = (Long) messageIdentifier.forIntrospection().getValue("databaseValue");
                    return findOfflineMessageByDataBaseId(comServerDAO, comJobExecutionModel, dataBaseId);
                default:
                    throw new UnsupportedOperationException("Unsupported identifier '" + messageIdentifier + "' of type " + messageIdentifier.forIntrospection().getTypeName() + ", command cannot be executed offline.");
            }
        } catch (ClassCastException | IndexOutOfBoundsException e) {
            throw CodingException.failedToParseIdentifierData(e, MessageSeeds.FAILED_TO_PARSE_IDENTIFIER_DATA, messageIdentifier.forIntrospection().getTypeName());
        }
    }

    private static OfflineDeviceMessage findOfflineMessageByDataBaseId(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, long databaseId) {
        List<OfflineDeviceMessage> deviceMessages = new ArrayList<>();
        deviceMessages.addAll(comJobExecutionModel.getOfflineDevice().getAllPendingDeviceMessages());
        deviceMessages.addAll(comJobExecutionModel.getOfflineDevice().getAllSentDeviceMessages());

        for (OfflineDeviceMessage deviceMessage : deviceMessages) {
            if (deviceMessage.getDeviceMessageId() == databaseId) {
                return deviceMessage;
            }
        }
        return null;
    }
}
