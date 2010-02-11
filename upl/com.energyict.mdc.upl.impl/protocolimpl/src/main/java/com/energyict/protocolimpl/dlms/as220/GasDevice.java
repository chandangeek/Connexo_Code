/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;

/**
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220 {

	private final GMeter		gMeter			= new GMeter(this);

    public GMeter getgMeter() {
		return gMeter;
	}

	private static final String PROP_GAS_DEVICE = "GasDevice";

	@Override
	public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		super.setProperties(properties);
	}

	@Override
	public List<String> getRequiredKeys() {
		List<String> requiredKeys = new ArrayList<String>();
		requiredKeys.addAll(super.getRequiredKeys());
		requiredKeys.add(PROP_GAS_DEVICE);
		return requiredKeys;
	}

}
