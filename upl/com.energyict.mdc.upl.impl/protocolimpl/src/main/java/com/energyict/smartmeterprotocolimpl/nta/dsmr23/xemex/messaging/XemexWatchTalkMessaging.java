package com.energyict.smartmeterprotocolimpl.nta.dsmr23.xemex.messaging;

import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.messages.RtuMessageCategoryConstants;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.messages.RtuMessageKeyIdConstants;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23Messaging;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 21/03/2014 - 9:24
 */
public class XemexWatchTalkMessaging extends Dsmr23Messaging {


    public XemexWatchTalkMessaging(MessageParser messageParser) {
        super(messageParser);
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        MessageCategorySpec catFirmware = getFirmwareCategory();
        MessageCategorySpec catP1Messages = getP1Category();
        MessageCategorySpec catActivityCal = getActivityCalendarCategory();
        MessageCategorySpec catTime = getTimeCategory();
        MessageCategorySpec catTestMessage = getTestCategory();
        MessageCategorySpec catXMLConfig = getXmlConfigCategory();
        MessageCategorySpec catAuthEncrypt = getAuthEncryptCategory();
        MessageCategorySpec catResetParameters = getAlarmResetCategory();

        categories.add(catFirmware);
        categories.add(catP1Messages);
        categories.add(catActivityCal);
        categories.add(catTime);
        categories.add(catTestMessage);
        categories.add(catXMLConfig);
        categories.add(catAuthEncrypt);
        categories.add(catResetParameters);
        return categories;
    }

    @Override
    public MessageCategorySpec getAuthEncryptCategory() {
        MessageCategorySpec catAuthEncrypt = new MessageCategorySpec(
      				RtuMessageCategoryConstants.AUTHENTICATEENCRYPT);
      		MessageSpec msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.NTA_CHANGEDATATRANSPORTENCRYPTIONKEY,
      				RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY, false);
      		catAuthEncrypt.addMessageSpec(msgSpec);
      		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.NTA_CHANGEDATATRANSPORTAUTHENTICATIONKEY,
      				RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY, false);
      		catAuthEncrypt.addMessageSpec(msgSpec);
      		msgSpec = addSecurityLevelMsg(RtuMessageKeyIdConstants.ACTIVATE_SECURITY,
      				RtuMessageConstant.AEE_ACTIVATE_SECURITY, true);
      		catAuthEncrypt.addMessageSpec(msgSpec);
      		return catAuthEncrypt;
    }
}
