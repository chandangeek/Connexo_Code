package com.elster.protocolimpl.dlms.messaging;

import com.elster.dlms.cosem.classes.class29.AutoConnectModeEnum;
import com.elster.dlms.cosem.simpleobjectmodel.Ek280Defs;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleAutoConnectObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.energyict.cbo.BusinessException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.messaging.MessageAttributeSpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.utils.MessagingTools;

import java.io.IOException;

/**
 * User: heuckeg
 * Date: 06.10.11
 * Time: 14:04
 */
public class DisableAutoConnectMessage extends AbstractDlmsMessage {

    /**
     * RtuMessage tags for the key change message
     */
    public static final String MESSAGE_TAG = "DisableAutoConnect";
    public static final String MESSAGE_DESCRIPTION = "Disable auto connect";
    public static final String ATTR_AUTOCONNECT_ID = "AutoConnectId";

    public DisableAutoConnectMessage(DlmsMessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    /**
     * Send the message  to the meter.
     *
     * @param messageEntry: the message containing the new keys
     * @throws com.energyict.cbo.BusinessException:
     *          when a parameter is null or too long
     */
    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {

        String autoConnectId = MessagingTools.getContentOfAttribute(messageEntry, ATTR_AUTOCONNECT_ID);
        validateMessageData(autoConnectId);
        try {
            write(autoConnectId);
        } catch (IOException e) {
            throw new BusinessException("Unable to disable auto connect: " + e.getMessage());
        }

    }

    private void write(String autoConnectId) throws IOException {

        SimpleCosemObjectManager objectManager = getExecutor().getDlms().getObjectManager();

        int acId = Integer.parseInt(autoConnectId);

        final SimpleAutoConnectObject autoConnect =
                objectManager.getSimpleCosemObject(Ek280Defs.AUTO_CONNECT_1.derive(1, acId),
                        SimpleAutoConnectObject.class);

        autoConnect.setMode(AutoConnectModeEnum.getValues()[0]);
    }


    private void validateMessageData(String autoConnectId) throws BusinessException {
        checkInt(autoConnectId, "AutoConnect id", 1, 2);
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);

        // Disable the value field in the EIServer message GUI
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");

        tagSpec.add(new MessageAttributeSpec(ATTR_AUTOCONNECT_ID, true));

        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }
}
