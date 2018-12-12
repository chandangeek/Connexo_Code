package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSUtils;


public class S4sRegisterTest {

	@Test
	public void getRegisterTest(){
		byte[] rawData = DLMSUtils.hexStringToByteArray("3138353132373530");
		byte[] unitTotalRegisterA = DLMSUtils.hexStringToByteArray("3334");
		
		S4sRegisterConfig totalRegisterUnit = new S4sRegisterConfig(unitTotalRegisterA);
		S4sRegister register = new S4sRegister(rawData, totalRegisterUnit);

		assertEquals(BigDecimal.valueOf(5721581),register.getRegisterQuantity().getAmount());
		assertEquals(Unit.get(BaseUnit.WATTHOUR,3), register.getRegisterQuantity().getUnit());
		
	}
	
}
