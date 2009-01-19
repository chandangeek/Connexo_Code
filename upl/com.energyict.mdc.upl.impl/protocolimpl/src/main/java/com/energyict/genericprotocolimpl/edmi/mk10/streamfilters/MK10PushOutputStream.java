package com.energyict.genericprotocolimpl.edmi.mk10.streamfilters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.energyict.genericprotocolimpl.edmi.mk10.parsers.MK10OutputStreamParser;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.tools.OutputStreamDecorator;

public class MK10PushOutputStream extends OutputStreamDecorator {

	private static final int DEBUG 				= 0;

	private ByteArrayOutputStream bufferOut 	= new ByteArrayOutputStream();

	public MK10PushOutputStream(OutputStream stream) {
		super(stream);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		bufferOut.write(b, off, len);
		updateBuffer();
	}

	public void write(byte[] b) throws IOException {
		bufferOut.write(b);
		updateBuffer();
	}

	public void write(int b) throws IOException {
		bufferOut.write(b);
		updateBuffer();
	}

	private void updateBuffer() throws IOException {
		MK10OutputStreamParser outputParser = new MK10OutputStreamParser();
		bufferOut.flush();
		outputParser.parse(bufferOut.toByteArray());
		
		if (outputParser.isValidPacket()) {
			 getStream().write(outputParser.getValidPacket());
			 getStream().flush();
			 
			 if (DEBUG >= 1)
				System.out.println(" OutputData = " + ProtocolUtils.getResponseData(bufferOut.toByteArray()));
			 
			 bufferOut.reset();
		}
	}
	
}
