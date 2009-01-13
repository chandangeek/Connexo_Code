package com.energyict.genericprotocolimpl.edmi.mk10;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.tools.OutputStreamDecorator;

public class MK10PushOutputStream extends OutputStreamDecorator {

	private static final int DEBUG 			= 0;
	private static final int BUFFER_SIZE 	= 1024;

	private static final byte STX			= 0x02;
	private static final byte ETX			= 0x03;
	
	private ByteBuffer buffer 				= ByteBuffer.allocate(BUFFER_SIZE);
	private boolean bufferEmpty				= true;
	private int bufferSize					= 0;
	
	public MK10PushOutputStream(OutputStream stream) {
		super(stream);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		for (int i = off; i < len; i++) {
			write(((int)b[i])& 0x000000FF);
		}
	}

	public void write(byte[] b) throws IOException {
		for (int i = 0; i < b.length; i++) {
			write(((int)b[i])& 0x000000FF);
		}
	}

	public void write(int b) throws IOException {
		if (b == STX) {
			buffer.clear();
			buffer.put((byte)STX);
		} else buffer.put((byte)(b & 0x000000FF));

		if (b == ETX) writeNextPacket();
	}

	private void writeNextPacket() throws IOException {
		byte[] tempBuffer;
		int bufferLength = buffer.position();
		
		if (bufferLength > 4) {
			tempBuffer = new byte[bufferLength - 4];
			
			System.out.println("bufferLength = " + bufferLength);
			System.out.println("tempBuffer.length = " + tempBuffer.length);

			buffer.position(1);
			buffer.get(tempBuffer);
		} else {
			buffer.position(0);
			tempBuffer = new byte[bufferLength];
			buffer.get(tempBuffer);
		}
		
		System.out.println(" protocolimpl -> MK10 = " + ProtocolUtils.getResponseData(tempBuffer));
		
		super.write(tempBuffer);
		buffer.clear();
	}
	
}
