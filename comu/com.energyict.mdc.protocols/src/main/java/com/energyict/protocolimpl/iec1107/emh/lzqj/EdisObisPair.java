/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.mdc.common.ObisCode;

/**
 * @author jme
 *
 */
public class EdisObisPair {

	private final ObisCode	obisCode;
	private final String	edisCode;

	/**
	 * @param obisCode
	 * @param edisCode
	 */
	public EdisObisPair(ObisCode obisCode, String edisCode) {
		this.edisCode = edisCode;
		this.obisCode = obisCode;
	}

	/**
	 * @param obisCode
	 * @param edisCode
	 */
	public EdisObisPair(String obisCode, String edisCode) {
		this(ObisCode.fromString(obisCode), edisCode);
	}

	/**
	 * @return
	 */
	public String getEdisCode() {
		return edisCode;
	}

	/**
	 * @return
	 */
	public ObisCode getObisCode() {
		return obisCode;
	}

}
