package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;

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
    public List<MessageCategorySpec> getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList<>();
        MessageCategorySpec catDisconnect = getConnectControlCategory();
        MessageCategorySpec catMbusSetup = getMbusSetupCategory();

        categories.add(catDisconnect);
        categories.add(catMbusSetup);
        return categories;
    }

    @Override
    public MessageCategorySpec getConnectControlCategory() {
        MessageCategorySpec catDisconnect = new MessageCategorySpec(RtuMessageCategoryConstants.DISCONNECTCONTROL);
        catDisconnect.addMessageSpec(
                addConnectControl(
                        RtuMessageKeyIdConstants.DISCONNECT,
                        RtuMessageConstant.DISCONNECT_LOAD,
                        false));
        catDisconnect.addMessageSpec(
                addConnectControl(
                        RtuMessageKeyIdConstants.CONNECT,
                        RtuMessageConstant.CONNECT_LOAD,
                        false));
        return catDisconnect;
    }

    @Override
    protected MessageSpec addConnectControl(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public MessageCategorySpec getMbusSetupCategory() {
        MessageCategorySpec catMbusSetup = new MessageCategorySpec(RtuMessageCategoryConstants.MBUSSETUP);
        catMbusSetup.addMessageSpec(
                addNoValueMsg(
                    RtuMessageKeyIdConstants.MBUSDECOMMISSION,
                    RtuMessageConstant.MBUS_DECOMMISSION,
                        false));
        catMbusSetup.addMessageSpec(
                addEncryptionkeys(
                        RtuMessageKeyIdConstants.MBUSENCRYPTIONKEY,
                        RtuMessageConstant.MBUS_ENCRYPTION_KEYS,
                        false));
        return catMbusSetup;
    }

}
