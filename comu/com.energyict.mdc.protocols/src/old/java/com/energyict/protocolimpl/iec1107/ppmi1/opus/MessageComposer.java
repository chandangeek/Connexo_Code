package com.energyict.protocolimpl.iec1107.ppmi1.opus;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.iec1107.ppmi1.PPMUtils;

import java.io.ByteArrayOutputStream;

class MessageComposer {

	private ByteArrayOutputStream	content	= new ByteArrayOutputStream();

	byte[] add(byte b) {
		content.write(b);
		return content.toByteArray();
	}

	MessageComposer add(CtrlChar ctrlChar) {
		content.write(ctrlChar.getByteValue());
		return this;
	}

	MessageComposer add(byte[] b) {
		content.write(b, 0, b.length);
		return this;
	}

	MessageComposer add(int i) {
		byte[] data = new byte[2];
		ProtocolUtils.val2BCDascii(i, data, 0);
		content.write(data, 0, 1);
		return this;
	}

	MessageComposer add(String aString) {
		char[] c = aString.toCharArray();
		for (int i = 0; i < c.length; i++) {
			content.write(c[i]);
		}
		return this;
	}

	byte[] toByteArray() {
		return this.content.toByteArray();
	}

	public String toString() {
		return toHexaString();
	}

	public String toHexaString() {
		StringBuffer result = new StringBuffer();
		byte[] contentArray = content.toByteArray();
		for (int i = 0; i < contentArray.length; i++) {
			result.append(PPMUtils.toHexaString(contentArray[i]) + " ");
		}
		return result.toString();
	}

}
