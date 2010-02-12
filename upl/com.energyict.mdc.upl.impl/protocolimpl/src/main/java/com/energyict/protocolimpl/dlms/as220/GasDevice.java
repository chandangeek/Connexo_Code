/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeterMessaging;

/**
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220 {

	private final GMeter	gMeter	= new GMeter(this);

	private int gasMeterSlot = -1;

    public GMeter getgMeter() {
		return gMeter;
	}

	@Override
	public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		validateProperties(properties);
		super.setProperties(properties);
	}

	private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		properties.list(System.out);

		String nodeId = properties.getProperty(MeterProtocol.NODEID);
		if (nodeId == null) {
			throw new MissingPropertyException("Excpected a value for " + MeterProtocol.NODEID + " but was null");
		}

		String[] nd = nodeId.split(":");
		for (int i = 0; i < nd.length; i++) {
			System.out.println("[" + i + "] " + nd[i]);
		}

	}

	@Override
	public List<String> getRequiredKeys() {
		List<String> requiredKeys = new ArrayList<String>();
		requiredKeys.addAll(super.getRequiredKeys());
		return requiredKeys;
	}

	@Override
	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return new GMeterMessaging(this).queryMessage(messageEntry);
	}

	@Override
	public List<MessageCategorySpec> getMessageCategories() {
		return new GMeterMessaging(this).getMessageCategories();
	}

}
