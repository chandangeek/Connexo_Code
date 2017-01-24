package com.energyict.protocolimpl.dlms.as220.gmeter;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * @author jme
 *
 */
public class GasRegister {

	private static final int	VALUE_MASK	= 0x07FFFFFF;
	private final GasDevice		gasDevice;

	public GasRegister(GasDevice gasDevice) {
		this.gasDevice = gasDevice;
	}

	private CosemObjectFactory getCosemObjectFactory() {
		return gasDevice.getCosemObjectFactory();
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		RegisterValue regVal = new RegisterValue(obisCode);
		ScalerUnit scalerUnit = getCosemObjectFactory().getCosemObject(obisCode).getScalerUnit();
		long value = getCosemObjectFactory().getCosemObject(obisCode).getValue() & VALUE_MASK;
        long interpretedValue = MBusValueTranslator.interpret(value, gasDevice.getDif());
        regVal.setQuantity(new Quantity(BigDecimal.valueOf(interpretedValue), scalerUnit.getUnitCode(), scalerUnit.getScaler()));
		return regVal;
	}
}
