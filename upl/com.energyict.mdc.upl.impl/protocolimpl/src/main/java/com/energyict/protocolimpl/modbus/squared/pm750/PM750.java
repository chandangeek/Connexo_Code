/*
 * PM750.java
 *
 * Created on 19 september 2005, 16:02
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.squared.pm750;

import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author Koen
 */
public class PM750 extends Modbus  {

	private MultiplierFactory multiplierFactory = null;

	@Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheDisConnect() throws IOException {
	}

	@Override
	public void setProperties(Properties properties) throws PropertyValidationException {
		super.setProperties(properties);
		setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty(PK_INTERFRAME_TIMEOUT, "50").trim()));
	}

    @Override
	public String getFirmwareVersion() throws IOException {
		return getRegisterFactory().getFunctionCodeFactory().getMandatoryReadDeviceIdentification().toString();
	}

    @Override
    public String getProtocolVersion() {
		return "$Date: 2015-04-09 09:16:13 +0200 (Thu, 09 Apr 2015) $";
	}

    @Override
	protected void initRegisterFactory() {
		setRegisterFactory(new RegisterFactory(this));
	}

    @Override
	public Date getTime() throws IOException {
		return new Date();
	}

    @Override
	public DiscoverResult discover(DiscoverTools discoverTools) {
		// discovery is implemented in the GenericModbusDiscover protocol
		return null;
	}

    @Override
	public BigDecimal getRegisterMultiplier(int address) throws IOException {
		return getMultiplierFactory().getMultiplier(address);
	}

	private MultiplierFactory getMultiplierFactory() {
		if (multiplierFactory == null) {
			multiplierFactory = new MultiplierFactory(this);
		}
		return multiplierFactory;
	}

}