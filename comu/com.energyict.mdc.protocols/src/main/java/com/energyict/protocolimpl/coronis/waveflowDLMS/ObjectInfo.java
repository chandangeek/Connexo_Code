/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.common.ObisCode;

public class ObjectInfo {

	private final int attribute;
	private final int classId;
	private final ObisCode obisCode;

	public ObjectInfo(int attribute, int classId, ObisCode obisCode) {
		super();
		this.attribute = attribute;
		this.classId = classId;
		this.obisCode = obisCode;
	}

	final int getAttribute() {
		return attribute;
	}

	final int getClassId() {
		return classId;
	}

	final ObisCode getObisCode() {
		return obisCode;
	}

}
