package com.energyict.genericprotocolimpl.edmi.mk10;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.energyict.genericprotocolimpl.edmi.mk10.packets.HeartbeatPacket;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacket;
import com.energyict.genericprotocolimpl.edmi.mk10.packets.PushPacketType;

public class HeartBeatPacketTest {

	private static final String	SERIALNUMBER	= "209435639";
	private static final String	PLANTNUMBER		= "E0909439";
	private static final String	EMEINUMBER		= "353167005962774";
	private static final String	SIMCARDNUMBER	= "8944122252171773618";

	private static final String PUSH_PACKET =
		"$8F$50$FF$E5$0C$7B$BB$F7$45$30$39$30$39$34$33$39" +
		"$00$33$35$33$31$36$37$30$30$35$39$36$32$37$37$34" +
		"$00$38$39$34$34$31$32$32$32$35$32$31$37$31$37$37" +
		"$33$36$31$38$00$0E$FD";

	@Test
	public void toStringTest() {
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(PUSH_PACKET));
		assertNotNull(packet.toString());
	}

	@Test
	public void packetTypeTest() {
		PushPacket packet = PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(PUSH_PACKET));
		assertEquals(HeartbeatPacket.class, packet.getClass());
		assertEquals(PushPacketType.HEARTBEAT, packet.getPushPacketType());
	}

	@Test
	public void serialNumberTest() {
		assertEquals(SERIALNUMBER, getHeartBeatPacket().getSerial());
	}

	@Test
	public void plantNumberTest() {
		assertEquals(PLANTNUMBER, getHeartBeatPacket().getPlantNumber());
	}

	@Test
	public void emeiNumberTest() {
		assertEquals(EMEINUMBER, getHeartBeatPacket().getGsmImei());
	}

	@Test
	public void simCardNumberTest() {
		assertEquals(SIMCARDNUMBER, getHeartBeatPacket().getGsmSimCardInfo());
	}

	private static HeartbeatPacket getHeartBeatPacket() {
		return (HeartbeatPacket) PushPacket.getPushPacket(MK10PushUtil.getBytesFromHexString(PUSH_PACKET));
	}

}
