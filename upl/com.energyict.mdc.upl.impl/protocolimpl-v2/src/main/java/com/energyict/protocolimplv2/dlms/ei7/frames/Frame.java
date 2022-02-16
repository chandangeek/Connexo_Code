package com.energyict.protocolimplv2.dlms.ei7.frames;

import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.Unsigned32;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Logger;

public class Frame implements Serializable {

	static class TimeoutGPRS {
		Unsigned32 sessionMaxDuration;
		Unsigned32 inactivityTimeout;
		Unsigned32 networkAttachTimeout;
	}

	public static final Logger LOGGER = Logger.getLogger(Frame.class.getName());

	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}

	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	static byte[] getByteArray(byte[] compactFrame, int offset, int length, AxdrType type) {
		byte[] array = new byte[length];
		System.arraycopy(compactFrame, offset, array, 1, length - 1);
		array[0] = type.getTag();
		return array;
	}
}
