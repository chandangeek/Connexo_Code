package com.energyict.protocolimpl.eig.nexus1272.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractReadCommand extends AbstractCommand {

	public byte[] getStartingAddress() {
		return startAddress;
	}

	public byte[] getNumRegisters() {
		return numRegisters;
	}

	public void setStartingAddress(byte[] startingAddress) {
		this.startAddress = startingAddress;
	}

	protected byte functionCode = 0x03;
	protected byte[] startAddress;
	protected byte[] numRegisters;
	
	public AbstractReadCommand(int transID) {
		super(transID);
		
		//length of read commands is always 6
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
		baos.write(startAddress);
		baos.write(numRegisters);
		return baos.toByteArray();
	}

	public void setNumRegisters(byte[] numRegisters) {
		this.numRegisters = numRegisters;
	}

	

}
