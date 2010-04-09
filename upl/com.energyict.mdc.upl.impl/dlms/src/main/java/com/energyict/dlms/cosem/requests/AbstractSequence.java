package com.energyict.dlms.cosem.requests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author jme
 *
 */
public abstract class AbstractSequence implements Sequence {

	private static final byte[]	END_OF_SEQUENCE	= new byte[] { 0x00 };

	public byte[] toByteArray() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		for (int i = 0; i < getFields().length; i++) {
			try {
				if (getFields()[i] != null) {
					bytes.write(getFields()[i].toByteArray());
				} else {
					bytes.write(END_OF_SEQUENCE);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bytes.toByteArray();
	}

}
