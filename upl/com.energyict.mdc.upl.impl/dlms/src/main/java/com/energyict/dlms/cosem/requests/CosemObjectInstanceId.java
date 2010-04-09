package com.energyict.dlms.cosem.requests;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class CosemObjectInstanceId extends OctetString {

	public CosemObjectInstanceId(ObisCode obisCode) {
		super(obisCode.getLN(), true);
	}

}
