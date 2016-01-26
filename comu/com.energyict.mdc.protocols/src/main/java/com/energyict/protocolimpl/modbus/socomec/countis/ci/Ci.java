package com.energyict.protocolimpl.modbus.socomec.countis.ci;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverResult;
import com.energyict.protocols.mdc.inbound.rtuplusserver.DiscoverTools;

import com.energyict.protocolimpl.modbus.core.Modbus;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * ProtocolImplementation for the Socomec Countis Ci protocol
 * <p>
 * <b>Manufacturor description:</b> The COUNTIS Ci is a pulse collector,
 * communicating via an RS485 link using JBUS/MODBUS protocol. Through 7
 * insulated on/off inputs, it counts the number of pulses from different energy
 * meters (water, air, gas, electricity,...)
 * </p>
 *
 * @author gna
 * @since 10-dec-2009
 *
 */
public class Ci extends Modbus {

	@Override
	public String getProtocolDescription() {
		return "Socomec Countis Ci Modbus";
	}

	private MultiplierFactory multiplierFactory=null;

	@Inject
	public Ci(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheDisConnect() throws IOException {
	}

	@Override
	protected List<String> doTheGetOptionalKeys() {
		return Collections.emptyList();
	}

	@Override
	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","50").trim()));
	}

	@Override
	protected void initRegisterFactory() {
		setRegisterFactory(new RegisterFactory(this));
	}

	public DiscoverResult discover(DiscoverTools discoverTools) {
		return null;
	}

    /**
     * @param address - the given address
     * @return the multiplier for the given address
     */
    public BigDecimal getRegisterMultiplier(int address) throws IOException {
        return getMultiplierFactory().getMultiplier(address);
    }

    /**
     * Getter for the {@link MultiplierFactory}
     *
     * @return the MulitpliereFactory
     */
    public MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
			multiplierFactory = new MultiplierFactory(this);
		}
        return multiplierFactory;
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public Date getTime() throws IOException {
    	return DateTime.parseDateTime(getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getReadHoldingRegistersRequest().getRegisters()).getMeterCalender().getTime();
    }

    public void setTime() throws IOException {
    	getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getWriteMultipleRegisters(DateTime.getCurrentDate());
    }

}