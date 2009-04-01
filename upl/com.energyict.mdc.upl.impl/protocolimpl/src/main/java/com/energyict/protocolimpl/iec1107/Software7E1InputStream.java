package com.energyict.protocolimpl.iec1107;

import java.io.IOException;
import java.io.InputStream;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.tools.InputStreamDecorator;

public class Software7E1InputStream extends InputStreamDecorator {

	private static final int BITMASK = 0x0000007F;
	private static final int DEBUG = 0;
	
	public Software7E1InputStream(InputStream stream) {
		super(stream);
	}

	public int read() throws IOException {
		int returnValue = super.read();
		if (returnValue != -1) {
			returnValue &= BITMASK;
			if (DEBUG >= 3) System.out.print("[" + (char)returnValue + "]" + "0x" + ProtocolUtils.buildStringHex(returnValue, 3) + " ");
		}
		return returnValue;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int returnValue = super.read(b, off, len);
		if (returnValue != -1) {
			for (int i = off; i < (off + len); i++) {
				b[i] &= BITMASK;
				if (DEBUG >= 3) System.out.print("[" + (char)b[i] + "]" + "0x" + ProtocolUtils.buildStringHex(b[i], 3) + " ");
			}
		}
		return returnValue;
	}

	public int read(byte[] b) throws IOException {
		int returnValue = super.read(b);
		if (returnValue != -1) {
			for (int i = 0; i < b.length; i++) {
				b[i] &= BITMASK;
				if (DEBUG >= 3) System.out.print("[" + (char)b[i] + "]" + "0x" + ProtocolUtils.buildStringHex(b[i], 3) + " ");
			}
		}
		return returnValue;
	}

}
