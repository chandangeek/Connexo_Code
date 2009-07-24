package com.energyict.genericprotocolimpl.webrtukp.messagehandling;

import java.util.ArrayList;
import java.util.List;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.messaging.MessageCategorySpec;

public class MeterMessages extends GenericMessaging{

	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		MessageCategorySpec catXMLConfig = getXmlConfigCategory();
		MessageCategorySpec catFirmware = getFirmwareCategory();
		MessageCategorySpec catP1Messages = getP1Category();
		MessageCategorySpec catDisconnect = getConnectControlCategory();
		MessageCategorySpec catLoadLimit = getLoadLimitCategory();
		MessageCategorySpec catActivityCal = getActivityCalendarCategory();
		MessageCategorySpec catTime = getTimeCategory();
		MessageCategorySpec catMakeEntries = getDataBaseEntriesCategory();
		MessageCategorySpec catGPRSModemSetup = getGPRSModemSetupCategory();
		MessageCategorySpec catTestMessage = getTestCategory();
		MessageCategorySpec catGlobalDisc = getGlobalResetCategory();
		MessageCategorySpec catWakeUp = getWakeupCategory();
		MessageCategorySpec catAuthEncrypt = getAuthEncryptCategory();
		
		categories.add(catXMLConfig);
		categories.add(catFirmware);
		categories.add(catP1Messages);
		categories.add(catDisconnect);
		categories.add(catLoadLimit);
		categories.add(catActivityCal);
		categories.add(catTime);
		categories.add(catMakeEntries);
		categories.add(catGPRSModemSetup);
		categories.add(catTestMessage);
		categories.add(catGlobalDisc);
		categories.add(catWakeUp);

		return categories;
	}
}
