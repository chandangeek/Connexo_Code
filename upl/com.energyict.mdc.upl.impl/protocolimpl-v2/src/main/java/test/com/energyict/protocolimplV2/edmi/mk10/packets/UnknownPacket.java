package test.com.energyict.protocolimplV2.edmi.mk10.packets;

public class UnknownPacket extends PushPacket {

	public UnknownPacket(byte[] packetData) {
		super(packetData);
	}

	@Override
	void doParse() {
		// TODO: implement correct parsing

	}

}
