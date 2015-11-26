package com.energyict.protocolimpl.modbus.socomec.countis.ci;

import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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
	
	private MultiplierFactory multiplierFactory=null;

	@Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheDisConnect() throws IOException {
	}

	@Override
	protected List doTheGetOptionalKeys() {
		return new ArrayList();
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
    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
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
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
    
    public Date getTime() throws IOException {
    	return DateTime.parseDateTime(getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getReadHoldingRegistersRequest().getRegisters()).getMeterCalender().getTime();
    }
    
    public void setTime() throws IOException {
    	getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getWriteMultipleRegisters(DateTime.getCurrentDate());
    }
    
    /**
     * Read the raw registers from the MobBus device
     * 
     * @param address - startAddress
     * @param length - the required data length
     * @return the registers from the device
     * @throws IOException if we couldn't read the data
     */
    int[] readRawValue(int address, int length)  throws IOException {
    	HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    }
    
}
