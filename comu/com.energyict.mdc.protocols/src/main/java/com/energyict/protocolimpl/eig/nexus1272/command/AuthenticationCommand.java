package com.energyict.protocolimpl.eig.nexus1272.command;


public class AuthenticationCommand extends AbstractWriteMultipleRegistersCommand {

	private byte[] password = null; 
	

	public AuthenticationCommand(int transID) {
		super(transID);
		startingAddress = new byte[] {(byte) 0xFF, 0x20};
		numSetPoints = new byte[] {0x00, 0x08};
		payloadByteCount = 0x10;
		length = 23;
	}

	@Override
	protected byte[] getPayload() {
		//we have to write 0x20 to 3 registers to authenticate even though the
		//password doesn't start until after these 3 registers.. I would assume this
		//is a bug on the meter side
		byte[] prePayload = {0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
		
		byte[] actualPayload = password;//new byte[] {0x32, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
		byte[] toRet = new byte[prePayload.length+actualPayload.length];
		System.arraycopy(prePayload, 0, toRet, 0, prePayload.length);
		System.arraycopy(actualPayload, 0, toRet, prePayload.length, actualPayload.length);
		return toRet;
	}
	
	public void setPassword(byte[] bs) {
		this.password = bs;
	}

}
