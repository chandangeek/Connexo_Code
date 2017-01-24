/**
 * 
 */
package com.energyict.protocolimpl.modbus.socomec.countis.ci;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author gna
 * @since 10-dec-2009
 *
 */
public class MultiplierFactory {
	
	private Ci counterCi;
	
	private static int[] fixedScalerValues = {2, 1, 0, 0, 0, 0};

	/**
	 * @param ci
	 */
	public MultiplierFactory(Ci ci) {
		this.counterCi = ci;
	}

	/**
	 * @param address
	 * @return
	 * @throws IOException 
	 */
	public BigDecimal getMultiplier(int address) throws IOException {
		BigDecimal bd = counterCi.getRegisterFactory().findRegister(RegisterFactory.energyScalers[address-1]).quantityValue().getAmount();
//		return new BigDecimal(Math.pow(10, fixedScalerValues[bd.intValue()]));
		return new BigDecimal(new BigInteger("1"),fixedScalerValues[bd.intValue()]);
	}

}
