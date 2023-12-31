/*
 * ObjectIdentification.java
 * Created on 16 oktober 2007, 14:50
 */

package com.energyict.dlms.cosem;

import com.energyict.obis.ObisCode;

/**
 * @author kvds
 */
public class ObjectIdentification {

	private ObisCode obisCode;
	private int			classId;

	/**
	 * Creates a new instance of ObjectIdentification
	 *
	 * @param obisCode
	 * @param classId
	 */
	public ObjectIdentification(ObisCode obisCode, int classId) {
		this.obisCode = obisCode;
		this.classId = classId;
	}

	/**
	 * Creates a new instance of ObjectIdentification
	 *
	 * @param obisCode
	 * @param classId
	 */
	public ObjectIdentification(String obisCode, int classId) {
		this(ObisCode.fromString(obisCode), classId);
	}

	/**
	 * Getter for the obisCode field
	 *
	 * @return the {@link com.energyict.obis.ObisCode}
	 */
	public ObisCode getObisCode() {
		return obisCode;
	}

	/**
	 * Getter for the classId field
	 *
	 * @return the class id
	 */
	public int getClassId() {
		return classId;
	}

}
