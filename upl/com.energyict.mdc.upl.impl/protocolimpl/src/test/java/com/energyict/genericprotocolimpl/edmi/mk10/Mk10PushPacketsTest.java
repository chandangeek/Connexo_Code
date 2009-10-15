package com.energyict.genericprotocolimpl.edmi.mk10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

import com.energyict.genericprotocolimpl.edmi.mk10.packets.CommissioningPacket;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacketType;

public class Mk10PushPacketsTest {

	private static final String COMMISSIONING_PACKER_VALID =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$00$33$35$36$31$38$37$30$33$30$30$30$32$35$31$38" +
		"$00$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$30$58$58$00$31$2E$33$36$20$00$04$02$6D$40$31$31" +
		"$61$66$2C$37$64$62$62$2C$38$2C$00$00$00$00$00$00" +
		"$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
		"$E4$37";

	private static final String COMMISSIONING_PACKER_INVALID =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$00$33$35$36$31$38$37$30$33$30$30$30$32$35$31$38" +
		"$00$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$30$58$58$00$31$2E$33$36$20$00$04$02$6D$40$31$31" +
		"$61$66$2C$37$64$62$62$2C$38$2C$00$00$00$00$00$00" +
		"$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
		"$E4$36";

	@Test
	public void testValidCommissioningPacket() {
		PushPacket packet = PushPacket.getPushPacket(getBytesFromHexString(COMMISSIONING_PACKER_VALID));
		assertNotNull(packet);
		assertTrue(packet.isValidPacket());
		assertEquals("209165151", packet.getSerial());
		assertEquals(209165151, packet.getSerialAsInt());
		assertEquals(packet.getActualCrc(), packet.getPacketCrc());
		assertEquals(58423, packet.getPacketCrc());
		assertEquals(58423, packet.getActualCrc());
		assertEquals(packet.getPushPacketType(), PushPacketType.COMMISSIONING);
		assertEquals(CommissioningPacket.class , packet.getClass());

		CommissioningPacket cPacket = (CommissioningPacket) packet;
		assertEquals("Mk10_SSC_0145", cPacket.getDeviceConfiguration());
		assertEquals("67267904", cPacket.getFirmwareEdition());
		assertEquals("1.36 ", cPacket.getFirmwareVersion());
		assertEquals("11af,7dbb,8,", cPacket.getGsmCellTowerInfo());
		assertEquals("356187030002518", cPacket.getGsmImei());
		assertEquals("", cPacket.getGsmSimCardInfo());
		assertEquals("10XX", cPacket.getMeterId());
		assertEquals("E0901346", cPacket.getPlantNumber());

	}

	@Test
	public void testInvalidCommissioningPacket() {
		PushPacket packet = PushPacket.getPushPacket(getBytesFromHexString(COMMISSIONING_PACKER_INVALID));
		assertNotNull(packet);
		assertFalse(packet.isValidPacket());
		assertEquals("209165151", packet.getSerial());
		assertEquals(209165151, packet.getSerialAsInt());
		assertNotSame(packet.getActualCrc(), packet.getPacketCrc());
		assertEquals(58422, packet.getPacketCrc());
		assertEquals(58423, packet.getActualCrc());
		assertEquals(packet.getPushPacketType(), PushPacketType.COMMISSIONING);
		assertEquals(CommissioningPacket.class , packet.getClass());

		CommissioningPacket cPacket = (CommissioningPacket) packet;
		assertEquals("Mk10_SSC_0145", cPacket.getDeviceConfiguration());
		assertEquals("67267904", cPacket.getFirmwareEdition());
		assertEquals("1.36 ", cPacket.getFirmwareVersion());
		assertEquals("11af,7dbb,8,", cPacket.getGsmCellTowerInfo());
		assertEquals("356187030002518", cPacket.getGsmImei());
		assertEquals("", cPacket.getGsmSimCardInfo());
		assertEquals("10XX", cPacket.getMeterId());
		assertEquals("E0901346", cPacket.getPlantNumber());

	}

	private byte[] getBytesFromHexString(String hexString) {
		ByteArrayOutputStream bb = new ByteArrayOutputStream();
		for (int i = 0; i < hexString.length(); i += 3) {
			bb.write(Integer.parseInt(hexString.substring(i + 1, i + 3), 16));
		}
		return bb.toByteArray();
	}

}
