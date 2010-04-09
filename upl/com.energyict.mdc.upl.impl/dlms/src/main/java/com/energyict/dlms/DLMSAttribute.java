package com.energyict.dlms;

import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;

/**
 * @author jme
 *
 */
public class DLMSAttribute {

	private final ObisCode	obisCode;
	private final int		attribute;
	private DLMSClassId		classId;

	/**
	 * @param obisCode
	 * @param attribute
	 */
	public DLMSAttribute(ObisCode obisCode, int attribute, DLMSClassId classId) {
		this.obisCode = obisCode;
		this.attribute = attribute;
		this.classId = classId;
	}

	/**
	 * @param obisCodeAsString
	 * @param attribute
	 */
	public DLMSAttribute(String obisCodeAsString, int attribute, DLMSClassId classId) {
		this(ObisCode.fromString(obisCodeAsString), attribute, classId);
	}

	/**
	 * @return
	 */
	public ObisCode getObisCode() {
		return obisCode;
	}

	/**
	 * @return
	 */
	public int getAttribute() {
		return attribute;
	}

	/**
	 * @return
	 */
	public DLMSClassId getClassId() {
		return classId;
	}

}
