package com.energyict.protocolimpl.eig.nexus1272.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractWriteMultipleRegistersCommand extends
		AbstractCommand {

	protected byte functionCode = 0x10;
	protected byte[] startingAddress;
	protected byte[] numSetPoints;
	protected byte payloadByteCount;
	
	public AbstractWriteMultipleRegistersCommand(int transID) {
		super(transID);
	}
	
	@Override
	public byte[] build() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] trans = intToByteArray(transactionID);
		baos.write(trans);
		byte[] protocol = intToByteArray(protocolID);
		baos.write(protocol);
		byte[] len = intToByteArray(length);
		baos.write(len);
		baos.write(unitID);
		baos.write(functionCode);
		baos.write(startingAddress);
		baos.write(numSetPoints);
		baos.write(payloadByteCount);
		baos.write(getPayload());
		return baos.toByteArray();
	}

	protected abstract byte[] getPayload();

}
