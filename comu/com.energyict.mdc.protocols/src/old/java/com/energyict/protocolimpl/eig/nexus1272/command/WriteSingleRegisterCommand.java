package com.energyict.protocolimpl.eig.nexus1272.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WriteSingleRegisterCommand extends AbstractCommand {

	protected byte functionCode = 0x06;
	protected byte[] address;
	
	protected byte[] data;
	
	public WriteSingleRegisterCommand(int transID) {
		super(transID);
		length = 6;
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
		baos.write(address);
		baos.write(data);
		return baos.toByteArray();
	}
	
	public void setAddress(byte[] address) {
		this.address = address;
	}

	public void setData(byte[] data) {
		this.data = data;
	}


}
