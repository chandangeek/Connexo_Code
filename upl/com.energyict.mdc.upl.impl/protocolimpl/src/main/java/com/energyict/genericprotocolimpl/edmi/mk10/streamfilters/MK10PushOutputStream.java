package com.energyict.genericprotocolimpl.edmi.mk10.streamfilters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.energyict.protocol.ProtocolException;
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
		throw new ProtocolException("Not implemented !!!");
	}

	public void write(byte[] b) throws IOException {
		throw new ProtocolException("Not implemented !!!");
	}

	public void write(int b) throws IOException {
		throw new ProtocolException("Not implemented !!!");
	}

	private void writeNextPacket() throws IOException {

	}
	
}
