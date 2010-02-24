/**
 *
 */
package com.energyict.protocolimpl.base;

import com.energyict.obis.ObisCode;

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
	 * Set the base {@link ObisCode} for this mapper
	 *
	 * @param obisCode
	 */
	void setBaseObjectObisCode(ObisCode obisCode);

	/**
	 * Check if a given {@link ObisCode} is mapped by this {@link DLMSAttributeMapper}
	 *
	 * @param obisCode
	 * @return true if the given {@link ObisCode} can be mapped
	 */
	boolean isObisCodeMapped(ObisCode obisCode);

}
