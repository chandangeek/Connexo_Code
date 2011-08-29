package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 29-aug-2011
 * Time: 14:11:36
 */
public class WebRTUZ3Messaging extends GenericMessaging implements MessageProtocol {

    /**
     * Indicates whether specific messages are allowed for the firmwareTeam
     */
    private static final boolean FIRMWAREBUILD = false;

    private final WebRTUZ3MessageExecutor messageExecutor;

    public WebRTUZ3Messaging(final WebRTUZ3MessageExecutor messageExecutor) {
        this.messageExecutor = messageExecutor;
    }

    /**
     * Abstract method to define your message categories *
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();

        categories.add(getAuthEncryptCategory());
        categories.add(getFirmwareCategory());
        categories.add(getActivityCalendarCategory());
        categories.add(getTimeCategory());
        categories.add(getConnectivityCategory());
        categories.add(getGlobalResetCategory());
        categories.add(getXmlConfigCategory());
        categories.add(getConnectControlCategory());

        if (FIRMWAREBUILD) {
            categories.add(getTestCategory());
            categories.add(getDataBaseEntriesCategory());
        }

        return categories;
    }

    /**
     * @return the messages for the ConnectivityCategory
     */
    private MessageCategorySpec getConnectivityCategory() {
        MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.CHANGECONNECTIVITY);
        MessageSpec msgSpec = addChangeGPRSSetup(
                RtuMessageKeyIdConstants.GPRSMODEMSETUP,
                RtuMessageConstant.GPRS_MODEM_SETUP, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addPhoneListMsg(RtuMessageKeyIdConstants.SETWHITELIST,
                RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
                RtuMessageConstant.WAKEUP_ACTIVATE, false);
        catGPRSModemSetup.addMessageSpec(msgSpec);
        msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_DEACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		return catGPRSModemSetup;
	}

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        // nothing to do
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageExecutor.executeMessageEntry(messageEntry);
    }
}
