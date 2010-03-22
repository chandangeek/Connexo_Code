package com.energyict.protocolimpl.dlms.as220.gmeter;

import java.io.IOException;
import java.math.BigDecimal;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.as220.GasDevice;

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
		regVal.setQuantity(new Quantity(BigDecimal.valueOf(value), scalerUnit.getUnitCode(), scalerUnit.getScaler()));
		return regVal;
	}

}
