package com.energyict.protocolimpl.modbus.socomec.countis.ci;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.protocolimpl.modbus.core.Modbus;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

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

	private MultiplierFactory multiplierFactory = null;

    public Ci(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
	protected void doTheConnect() throws IOException {
	}

	@Override
	protected void doTheDisConnect() throws IOException {
	}

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
		setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "50").trim()));
	}

	@Override
	protected void initRegisterFactory() {
		setRegisterFactory(new RegisterFactory(this));
	}

    @Override
    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        return getMultiplierFactory().getMultiplier(address);
    }

    private MultiplierFactory getMultiplierFactory() {
        if (multiplierFactory == null) {
			multiplierFactory = new MultiplierFactory(this);
		}
        return multiplierFactory;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public Date getTime() throws IOException {
    	return DateTime.parseDateTime(getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getReadHoldingRegistersRequest().getRegisters()).getMeterCalender().getTime();
    }

    @Override
    public void setTime() throws IOException {
    	getRegisterFactory().findRegister(RegisterFactory.currentDateTime).getWriteMultipleRegisters(DateTime.getCurrentDate());
    }

}