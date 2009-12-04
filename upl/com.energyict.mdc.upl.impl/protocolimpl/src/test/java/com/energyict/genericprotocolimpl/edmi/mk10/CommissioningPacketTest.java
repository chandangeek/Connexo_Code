package com.energyict.genericprotocolimpl.edmi.mk10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.energyict.genericprotocolimpl.edmi.mk10.packets.CommissioningPacket;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacketType;

public class CommissioningPacketTest {

	private static final String COMMISSIONING_PACKER_VALID =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$00$33$35$36$31$38$37$30$33$30$30$30$32$35$31$38" +
		"$00$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$30$58$58$00$31$2E$33$36$20$00$04$02$6D$40$31$31" +
		"$61$66$2C$37$64$62$62$2C$38$2C$00$00$00$00$00$00" +
		"$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
		"$E4$37";

	private static final String COMMISSIONING_PACKER_INVALID_CRC =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$00$33$35$36$31$38$37$30$33$30$30$30$32$35$31$38" +
		"$00$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$30$58$58$00$31$2E$33$36$20$00$04$02$6D$40$31$31" +
		"$61$66$2C$37$64$62$62$2C$38$2C$00$00$00$00$00$00" +
		"$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00$00" +
		"$E4$36";

	private static final String COMMISSIONING_PACKER_INVALID_MISSING =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$01$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$01$4D$6B$31$30$5F$53$53$43$5F$30$31$34$35$00$31" +
		"$07$97";

	private static final String COMMISSIONING_PACKER_INVALID_SHORT =
		"$8F$50$FF$E3$0C$77$9B$5F$45$30$39$30$31$33$34$36" +
		"$E4$36";

	@Test
	public void testValidCommissioningPacket() {
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(COMMISSIONING_PACKER_VALID));
		assertNotNull(packet);
		assertTrue(packet.isValidPacket());
		assertEquals("209165151", packet.getSerial());
		assertEquals(209165151, packet.getSerialAsInt());
		assertEquals(packet.getActualCrc(), packet.getPacketCrc());
		assertEquals(58423, packet.getPacketCrc());
		assertEquals(58423, packet.getActualCrc());
		assertEquals(packet.getPushPacketType(), PushPacketType.COMMISSIONING);
		assertNotNull(packet.getPushPacketType().getDescription());
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
	public void testInvalidCrcCommissioningPacket() {
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(COMMISSIONING_PACKER_INVALID_CRC));
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

	@Test
	public void testInvalidShortCommissioningPacket() {
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(COMMISSIONING_PACKER_INVALID_SHORT));
		assertNotNull(packet);
		assertFalse(packet.isValidPacket());
		assertEquals("209165151", packet.getSerial());
		assertEquals(209165151, packet.getSerialAsInt());
		assertNotSame(packet.getActualCrc(), packet.getPacketCrc());
		assertEquals(58422, packet.getPacketCrc());
		assertEquals(60104, packet.getActualCrc());
		assertEquals(packet.getPushPacketType(), PushPacketType.COMMISSIONING);
		assertEquals(CommissioningPacket.class , packet.getClass());

		CommissioningPacket cPacket = (CommissioningPacket) packet;
		assertNull(cPacket.getDeviceConfiguration());
		assertNull(cPacket.getFirmwareEdition());
		assertNull(cPacket.getFirmwareVersion());
		assertNull(cPacket.getGsmCellTowerInfo());
		assertNull(cPacket.getGsmImei());
		assertNull(cPacket.getGsmSimCardInfo());
		assertNull(cPacket.getMeterId());
		assertNull(cPacket.getPlantNumber());

	}

	@Test
	public void testInvalidMissingCommissioningPacket() {
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(COMMISSIONING_PACKER_INVALID_MISSING));
		assertNotNull(packet);
		assertFalse(packet.isValidPacket());
		assertEquals("209165151", packet.getSerial());
		assertEquals(209165151, packet.getSerialAsInt());
		assertNotSame(packet.getActualCrc(), packet.getPacketCrc());
		assertEquals(1943, packet.getPacketCrc());
		assertEquals(1943, packet.getActualCrc());
		assertEquals(packet.getPushPacketType(), PushPacketType.COMMISSIONING);
		assertEquals(CommissioningPacket.class , packet.getClass());

		CommissioningPacket cPacket = (CommissioningPacket) packet;
		assertNull(cPacket.getDeviceConfiguration());
		assertNull(cPacket.getFirmwareEdition());
		assertNull(cPacket.getFirmwareVersion());
		assertNull(cPacket.getGsmCellTowerInfo());
		assertNull(cPacket.getGsmImei());
		assertNull(cPacket.getGsmSimCardInfo());
		assertNull(cPacket.getMeterId());
		assertNull(cPacket.getPlantNumber());

	}

	@Test
	public void testToString() {
		// Small check if the toString doesn't generate NPE's or something like this
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(COMMISSIONING_PACKER_INVALID_MISSING));
		assertNotNull(packet);
		assertNotNull(packet.toString());
	}

}
