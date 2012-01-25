package com.energyict.genericprotocolimpl.webrtuz3.messagehandling;

import com.energyict.genericprotocolimpl.common.messages.GenericMessaging;
import com.energyict.protocol.messaging.MessageCategorySpec;

import java.util.ArrayList;
import java.util.List;
/**
 * Define the different messageCategories you want to use in the protocol.
 * @author gna
 *
 */
public class MbusMessages extends GenericMessaging{

	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		categories.add(getConnectControlCategory());
		categories.add(getMbusSetupCategory());
		categories.add(getActivityCalendarCategory());
		return categories;
	}

}
