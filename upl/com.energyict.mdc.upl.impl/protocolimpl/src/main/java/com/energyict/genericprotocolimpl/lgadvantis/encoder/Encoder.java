package com.energyict.genericprotocolimpl.lgadvantis.encoder;

import com.energyict.dlms.axrdencoding.AbstractDataType;

/**
 * Encode an Object as DLMS AXDR encoding.
 * 
 * Or in Javanese: A specific encoder knows how to convert a specific object to
 * an AbstractDataType (subclass).
 * 
 * 
 */

public interface Encoder {

	AbstractDataType encode(Object value);

}
