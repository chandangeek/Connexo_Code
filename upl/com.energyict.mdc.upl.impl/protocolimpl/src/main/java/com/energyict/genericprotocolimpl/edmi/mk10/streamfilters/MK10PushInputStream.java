package com.energyict.genericprotocolimpl.edmi.mk10.streamfilters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.energyict.genericprotocolimpl.edmi.mk10.parsers.MK10InputStreamParser;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.tools.InputStreamDecorator;

public class MK10PushInputStream extends InputStreamDecorator {

	private static final int DEBUG 				= 0;
	private static final int BYTEMASK 			= 0x000000FF;

	private byte[] packetBuffer					= new byte[0];
	private byte[] buffer						= null;
	private ByteArrayInputStream bufferIn 		= new ByteArrayInputStream(packetBuffer);
	private ByteArrayOutputStream bufferOut 	= new ByteArrayOutputStream();

	public MK10PushInputStream(InputStream stream) {
		super(stream);
	}

	public int read() throws IOException {
		int returnValue = 0;
		updateBuffer();
		returnValue = bufferIn.read();
		return returnValue;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		throw new ProtocolException("Not implemented !!!!");
	}

	public int read(byte[] b) throws IOException {
		throw new ProtocolException("Not implemented !!!!");
	}

	public int available() throws IOException {
		updateBuffer();
		return bufferIn.available();
	}

	/*
	 * Private getters, setters and methods
	 */

	private void updateBuffer() throws IOException {
		MK10InputStreamParser streamParser = new MK10InputStreamParser();

		while (super.getStream().available() > 0) {

			if (super.getStream().available() > 0) {
				byte bytevalue = (byte)(super.getStream().read() & BYTEMASK);
				bufferOut.write(bytevalue);
				//bufferOut.flush();
			}

			streamParser.parse(bufferOut.toByteArray(), isLastByte());
			if (streamParser.isValidPacket()) updateInputStream();

		}
	}

	private boolean isLastByte() throws IOException {
		boolean returnValue = (super.getStream().available() > 0);
		return !returnValue;
	}
	
	private void updateInputStream() throws IOException {
		if (DEBUG >= 1)	System.out.println("** updateInputStream() **");

		byte[] tempBuffer = new byte[bufferIn.available()];
		MK10InputStreamParser streamParser = new MK10InputStreamParser();
		int length = streamParser.parse(bufferOut.toByteArray(), isLastByte());
		bufferIn.read(tempBuffer);

		if (streamParser.isPushPacket()) {
			this.buffer = tempBuffer;
		} else {
			this.buffer = ProtocolUtils.concatByteArrays(tempBuffer, streamParser.getValidPacket());
		}

		this.bufferOut.reset();
		this.bufferIn = new ByteArrayInputStream(this.buffer);
	}
}
