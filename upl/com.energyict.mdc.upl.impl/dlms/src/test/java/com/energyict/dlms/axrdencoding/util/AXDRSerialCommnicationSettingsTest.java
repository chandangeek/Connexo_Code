/**
 *
 */
package com.energyict.dlms.axrdencoding.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.energyict.cbo.SerialCommunicationSettings;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * @author jme
 *
 */
public class AXDRSerialCommnicationSettingsTest {

	private static final int	SPEED		= 9600;
	private static final int	DATABITS	= 8;
	private static final char	PARITY		= 'N';
	private static final int	STOPBITS	= 1;

	private static final SerialCommunicationSettings	SERIAL_COMMUNICATION_SETTINGS	= new SerialCommunicationSettings(SPEED, DATABITS, PARITY, STOPBITS);
	private static final Structure STRUCTURE;

	static {
		STRUCTURE = new Structure();
		STRUCTURE.addDataType(new Integer8(DATABITS));
		STRUCTURE.addDataType(new Integer8(STOPBITS));
		STRUCTURE.addDataType(new Integer32(SPEED));
		STRUCTURE.addDataType(new Integer8((int) PARITY));
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRSerialCommunicationSettings#encode(com.energyict.cbo.SerialCommunicationSettings)}.
	 */
	@Test
	public final void testEncode() {
		assertNull(AXDRSerialCommunicationSettings.encode(null));
		assertNotNull(AXDRSerialCommunicationSettings.encode(SERIAL_COMMUNICATION_SETTINGS));
		assertEquals(SERIAL_COMMUNICATION_SETTINGS.getDataBits(), AXDRSerialCommunicationSettings.encode(SERIAL_COMMUNICATION_SETTINGS).getDataType(0).getInteger8().getValue());
		assertEquals(SERIAL_COMMUNICATION_SETTINGS.getStopBits(), AXDRSerialCommunicationSettings.encode(SERIAL_COMMUNICATION_SETTINGS).getDataType(STOPBITS).getInteger8().getValue());
		assertEquals(SERIAL_COMMUNICATION_SETTINGS.getSpeed(), AXDRSerialCommunicationSettings.encode(SERIAL_COMMUNICATION_SETTINGS).getDataType(2).getInteger32().getValue());
		assertEquals(SERIAL_COMMUNICATION_SETTINGS.getParity(), AXDRSerialCommunicationSettings.encode(SERIAL_COMMUNICATION_SETTINGS).getDataType(3).getInteger8().getValue());
		assertEquals(STRUCTURE.toString(), AXDRSerialCommunicationSettings.encode(AXDRSerialCommunicationSettings.decode(STRUCTURE)).toString());
	}

	/**
	 * Test method for {@link com.energyict.dlms.axrdencoding.util.AXDRSerialCommunicationSettings#decode(com.energyict.dlms.axrdencoding.AbstractDataType)}.
	 */
	@Test
	public final void testDecode() {
		assertNull(AXDRSerialCommunicationSettings.decode(null));
		assertNull(AXDRSerialCommunicationSettings.decode(new NullData()));
		assertNull(AXDRSerialCommunicationSettings.decode(new Integer8(0)));
		assertNotNull(AXDRSerialCommunicationSettings.decode(STRUCTURE));
		assertEquals(SPEED, AXDRSerialCommunicationSettings.decode(STRUCTURE).getSpeed());
		assertEquals(DATABITS, AXDRSerialCommunicationSettings.decode(STRUCTURE).getDataBits());
		assertEquals(PARITY, AXDRSerialCommunicationSettings.decode(STRUCTURE).getParity());
		assertEquals(STOPBITS, AXDRSerialCommunicationSettings.decode(STRUCTURE).getStopBits());
		assertEquals(SERIAL_COMMUNICATION_SETTINGS, AXDRSerialCommunicationSettings.decode(AXDRSerialCommunicationSettings.encode(SERIAL_COMMUNICATION_SETTINGS)));
	}

}
