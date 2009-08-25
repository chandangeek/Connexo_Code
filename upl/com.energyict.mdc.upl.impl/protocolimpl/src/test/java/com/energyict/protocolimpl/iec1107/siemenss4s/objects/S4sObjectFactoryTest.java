package com.energyict.protocolimpl.iec1107.siemenss4s.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSUtils;


public class S4sObjectFactoryTest {

	@Test
	public void objectsTest(){
		byte[] p = DLMSUtils.hexStringToByteArray("34");
		S4sIntegrationPeriod totIntegrationPeriod = new S4sIntegrationPeriod(p);
		assertEquals(1800, totIntegrationPeriod.getInterval());
		
		byte[] unitTotalRegisterA = DLMSUtils.hexStringToByteArray("3334");
		S4sRegisterConfig totalRegisterUnit = new S4sRegisterConfig(unitTotalRegisterA);
		assertEquals(Unit.get(BaseUnit.WATTHOUR,3), totalRegisterUnit.getUnit());
		assertTrue("Import".equalsIgnoreCase(totalRegisterUnit.getType()));
		assertEquals(0, totalRegisterUnit.getDecimals());
		
		byte[] b = DLMSUtils.hexStringToByteArray("36343933363331343233313339333333353330303030303030303030");
		S4sSerialNumber s = new S4sSerialNumber(b);
		assertTrue("F96A21935".equalsIgnoreCase(s.getSerialNumber()));
		
		byte[] pp = DLMSUtils.hexStringToByteArray("433133");
		S4sProfilePointer profilePointer = new S4sProfilePointer(pp);
		assertEquals(0x31C0/4, profilePointer.getCurrentPointer());
		pp = DLMSUtils.hexStringToByteArray("413733");
		profilePointer = new S4sProfilePointer(pp);
		assertEquals(0x37A0/4, profilePointer.getCurrentPointer());
		pp = DLMSUtils.hexStringToByteArray("454137");
		profilePointer = new S4sProfilePointer(pp);
		assertEquals(0x7AE0/4, profilePointer.getCurrentPointer());
		
		byte[] date = new byte[]{56, 48, 57, 48, 52, 50};
		byte[] time = new byte[]{51, 53, 51, 48, 55, 48};
		S4sDateTime dateTime = new S4sDateTime(date, time);
		// 1251097433 is equals to the following time: 07:03:53 24/08/2009 GMT
		assertEquals(1251097433, dateTime.getMeterTime().getTimeInMillis()/1000);
		
		
	}
}
