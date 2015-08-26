package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocol.messaging.MessageTagSpec;
import com.energyict.protocol.messaging.MessageValueSpec;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessaging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 24/03/2014 - 11:40
 */
public class XemexWatchTalkMbusMessaging extends Dsmr23MbusMessaging {

    public XemexWatchTalkMbusMessaging() {
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<MessageCategorySpec>();
        MessageCategorySpec catDisconnect = getConnectControlCategory();
        MessageCategorySpec catMbusSetup = getMbusSetupCategory();

        categories.add(catDisconnect);
        categories.add(catMbusSetup);
        return categories;
    }

    @Override
    public MessageCategorySpec getConnectControlCategory() {
        MessageCategorySpec catDisconnect = new MessageCategorySpec(
                RtuMessageCategoryConstants.DISCONNECTCONTROL);
        MessageSpec msgSpec = addConnectControl(
                RtuMessageKeyIdConstants.DISCONNECT,
                RtuMessageConstant.DISCONNECT_LOAD, false);
        catDisconnect.addMessageSpec(msgSpec);
        msgSpec = addConnectControl(RtuMessageKeyIdConstants.CONNECT,
                RtuMessageConstant.CONNECT_LOAD, false);
        catDisconnect.addMessageSpec(msgSpec);
        return catDisconnect;
    }

    @Override
    protected MessageSpec addConnectControl(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public MessageCategorySpec getMbusSetupCategory() {
        MessageCategorySpec catMbusSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.MBUSSETUP);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.MBUSDECOMMISSION,
                RtuMessageConstant.MBUS_DECOMMISSION, false);
        catMbusSetup.addMessageSpec(msgSpec);
        msgSpec = addEncryptionkeys(RtuMessageKeyIdConstants.MBUSENCRYPTIONKEY,
                RtuMessageConstant.MBUS_ENCRYPTION_KEYS, false);
        catMbusSetup.addMessageSpec(msgSpec);
        return catMbusSetup;
    }

}
