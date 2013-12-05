package com.energyict.dlms.axrdencoding.util;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public final class AXDRProperties {

	/**
	 * Hide the constructor for a utility class. All the methods are static
	 */
	private AXDRProperties() {
	}

	/**
	 * @param properties
	 * @return
	 * @throws IOException
	 */
	public static Array encode(Properties properties) throws IOException {
		Array array = new Array();
		if (properties != null) {
			Enumeration<Object> e = properties.keys();
			while (e.hasMoreElements()) {
				Object key = e.nextElement();
				String value = (String) properties.get(key);
				Structure keyValue = new Structure();
				if (key instanceof Integer) {
					keyValue.addDataType(new Integer32(((Integer) key).intValue()));
				} else if (key instanceof String) {
					keyValue.addDataType(OctetString.fromString((String) key));
				} else {
					throw new IOException("Invalid key type for " + key.getClass().getName());
				}
				keyValue.addDataType(OctetString.fromString(value));
				array.addDataType(keyValue);
			}
		}
		return array;
	}

	/**
	 * @param dataType
	 * @return
	 * @throws IOException
	 */
	public static Properties decode(AbstractDataType dataType) throws IOException {
		Properties properties = new Properties();
		if ((dataType != null) && (dataType.isArray())) {
			Array array = dataType.getArray();
			for (int i = 0; i < array.nrOfDataTypes(); i++) {
				Structure keyValue = array.getDataType(i).getStructure();
				Object key = null;
				if (keyValue.getDataType(0).isOctetString()) {
					key = keyValue.getDataType(0).getOctetString().stringValue();
				} else if (keyValue.getDataType(0).isInteger32()) {
					key = Integer.valueOf(keyValue.getDataType(0).intValue());
				} else {
					throw new IOException("Invalid key type for " + keyValue.getDataType(0).getClass().getName());
				}

				if (!keyValue.getDataType(1).isOctetString()) {
					throw new IOException("Invalid value type for " + keyValue.getDataType(1).getClass().getName() + ". Should be OctetString");
				}

				String value = keyValue.getDataType(1).getOctetString().stringValue();
				properties.put(key, value);
			}
		}
		return properties;
	}

}
