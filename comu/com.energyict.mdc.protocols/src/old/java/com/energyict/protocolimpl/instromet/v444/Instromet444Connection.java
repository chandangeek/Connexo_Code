package com.energyict.protocolimpl.instromet.v444;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.instromet.connection.Command;
import com.energyict.protocolimpl.instromet.connection.InstrometConnection;
import com.energyict.protocolimpl.instromet.connection.ResponseReceiver;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Instromet444Connection extends InstrometConnection {

	private static final int DEBUG = 0;

	public Instromet444Connection(InputStream inputStream,
			OutputStream outputStream,
			int timeout,
			int maxRetries,
			long forcedDelay,
			int echoCancelling,
			HalfDuplexController halfDuplexController) throws ConnectionException {
		super(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling,halfDuplexController);
	}

	protected void writeAdress(Command command)  {
		ByteArrayOutputStream outputStream = getOutputStream();
		int address = command.getStartAddress();
		outputStream.write(address&0xFF);
		outputStream.write((address>>8)&0xFF);
		outputStream.write((address>>16)&0xFF);
		outputStream.write((address>>24)&0xFF);
	}

	protected void writeLength(Command command)  {
		ByteArrayOutputStream outputStream = getOutputStream();
		int length = command.getLength();
		outputStream.write(length&0xFF);
		outputStream.write((length>>8)&0xFF);
	}

	protected void writeCommId(Command command)  {
		ByteArrayOutputStream outputStream = getOutputStream();
		int commId = getNodeAddress();
		//System.out.println("commId = " + commId);
		outputStream.write(commId&0xFF);
		outputStream.write((commId>>8)&0xFF);
	}

	protected void writeCrc() {
		ByteArrayOutputStream outputStream = getOutputStream();
		int crc = CRCGenerator.calcCCITTCRCReverse(outputStream.toByteArray());
		//nt crc = 0;
		outputStream.write((crc>>8)&0xFF);
		outputStream.write(crc&0xFF);
	}

	public void checkCrc(byte[] data, byte[] crc) throws IOException {
		int crcValueFound = ProtocolUtils.getInt(crc, 0, 2);
		int crcCalculated = CRCGenerator.calcCCITTCRCReverse(ProtocolUtils.getSubArray2(data, 0, data.length - 2));
		if (DEBUG >= 1) {
			System.out.println("crcValueFound = " + crcValueFound);
			System.out.println("crcCalculated = " + crcCalculated);
		}
		if (crcValueFound != crcCalculated) {
			throw new IOException("invalid crc");
		}
	}

	protected ResponseReceiver doGetResponseReceiver() {
		return new ResponseReceiver444(this, getTimeout());
	}

}
