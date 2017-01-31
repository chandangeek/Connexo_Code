/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.base;

import com.energyict.mdc.common.ObisCode;

/**
 * Classes who are mapping different attributes from an base dlms class to
 * different registers, should implement this interface.
 *
 * @author jme
 */
public interface DLMSAttributeMapper extends ObiscodeMapper {

	/**
	 * Get the base {@link ObisCode} for this mapper
	 *
	 * @return
	 */
	ObisCode getBaseObjectObisCode();

	/**
	 * Check if a given {@link ObisCode} is mapped by this {@link DLMSAttributeMapper}
	 *
	 * @param obisCode
	 * @return true if the given {@link ObisCode} can be mapped
	 */
	boolean isObisCodeMapped(ObisCode obisCode);

	/**
	 * Get an array of supported attribute id's
	 * @return
	 */
	int[] getSupportedAttributes();
}
