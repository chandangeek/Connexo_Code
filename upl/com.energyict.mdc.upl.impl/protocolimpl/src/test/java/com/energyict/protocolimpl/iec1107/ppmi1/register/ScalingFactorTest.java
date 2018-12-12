package com.energyict.protocolimpl.iec1107.ppmi1.register;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.junit.Test;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.ppmi1.register.ScalingFactor;

public class ScalingFactorTest {

	@Test
	public void testScalingFactorParse() {
		assertEquals(ScalingFactor.REGISTER_CATEGORY_0 , ScalingFactor.parse((byte) 0x00));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_1 , ScalingFactor.parse((byte) 0x01));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_2 , ScalingFactor.parse((byte) 0x02));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_3 , ScalingFactor.parse((byte) 0x03));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_4A , ScalingFactor.parse((byte) 0x04));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_4B , ScalingFactor.parse((byte) 0x14));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_5A , ScalingFactor.parse((byte) 0x05));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_5B , ScalingFactor.parse((byte) 0x15));
		assertEquals(ScalingFactor.REGISTER_CATEGORY_6 , ScalingFactor.parse((byte) 0x06));
		assertNull(ScalingFactor.parse((byte) 0x07));
	}

	@Test
	public void testToString() {
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_0.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_1.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_2.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_3.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_4A.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_4B.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_5A.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_5B.toString());
		assertNotNull(ScalingFactor.REGISTER_CATEGORY_6.toString());
	}

	@Test
	public void testConstructor() {
		String description = "testDescription";
		String registerFactor = "0.1";
		Unit registerUnit = Unit.get("kW");
		String profileFactor = "0.01";
		ScalingFactor sf = new ScalingFactor(description, registerFactor, registerUnit, profileFactor);
		assertEquals(description, sf.getDescription());
		assertEquals(new BigDecimal(registerFactor), sf.getRegisterScaleFactor());
		assertEquals(new BigDecimal(profileFactor), sf.getProfileFactor());
		assertEquals(Unit.get("kW").getScale(), sf.getUnitScale());
		assertEquals(new BigDecimal("0.01"), sf.toProfileNumber(1));
		assertEquals(new Quantity(new BigDecimal("0.1"), Unit.get("kW")), sf.toRegisterQuantity(1));
	}

}
