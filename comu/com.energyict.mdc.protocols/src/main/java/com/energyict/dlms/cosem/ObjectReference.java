/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;

/**
 *
 * @author Koen
 */
public class ObjectReference {

	public static final int LN_REFERENCE = 0;
	public static final int SN_REFERENCE = 1;
    public static final int LN_LENGTH = 6;

    private byte[] ln = null;
	private int classId = -1;
	private int sn = -1;
	private int reference = -1;

	/**
	 * Creates a new instance of ObjectReference
	 *
	 * @param ln
	 */
	public ObjectReference(byte[] ln) {
		this(ln, -1);
	}

	/**
	 * Creates a new instance of ObjectReference
	 *
	 * @param ln
	 * @param classId
	 */
	public ObjectReference(byte[] ln, int classId) {
		reference = LN_REFERENCE;
		this.ln = ln.clone();
		this.classId = classId;
	}

	/**
	 * Creates a new instance of ObjectReference
	 *
	 * @param sn
	 */
	public ObjectReference(int sn) {
		reference = SN_REFERENCE;
		this.sn = sn;
	}

	/**
	 * Check if the long name is an abstract LN
	 *
	 * @return
	 */
	public boolean isAbstract() {
		return ln == null ? false : ((ln[0] == 0) && (ln[1] == 0));
	}

	/**
	 * Check if short name referencing is used
	 *
	 * @return true if short name referencing is used.
	 */
	public boolean isSNReference() {
		return reference == SN_REFERENCE;
	}

	/**
	 * Check if long name referencing is used
	 *
	 * @return true if long name referencing is used.
	 */
	public boolean isLNReference() {
		return reference == LN_REFERENCE;
	}

	/**
	 * Getter for property ln.
	 *
	 * @return Value of property ln.
	 */
	public byte[] getLn() {
		return this.ln;
	}

	/**
	 * Getter for property sn.
	 *
	 * @return Value of property sn.
	 */
	public int getSn() {
		return sn;
	}

	/**
	 * Getter for property classId.
	 *
	 * @return Value of property classId.
	 */
	public int getClassId() {
		return classId;
	}

    public ObisCode getObisCode() {
        if ((getLn() != null) && (getLn().length == LN_LENGTH)) {
            return ObisCode.fromByteArray(getLn());
        }
        return null;
    }

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(sn != -1 ? sn + ", " : "");
		sb.append(classId != -1 ? classId + ", " : "");
		if (ln != null) {
			for (int i = 0; i < ln.length; i++) {
				sb.append(ln[i]);
				if (i != (ln.length - 1)) {
					sb.append(".");
				}
			}
		}
		return sb.toString();
	}

}
