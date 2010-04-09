package com.energyict.dlms.cosem.requests;

import com.energyict.dlms.axrdencoding.AbstractDataType;

/**
 * @author jme
 *
 */
public class SelectiveAccessDescriptor extends AbstractSequence {

	private AccessSelector accessSelector;
	private AbstractDataType accessParameters;

	public Field[] getFields() {
		return new Field[] {
				accessSelector,
				accessParameters
		};
	}

	/**
	 * @return the accessSelector
	 */
	public AccessSelector getAccessSelector() {
		return accessSelector;
	}
	/**
	 * @param accessSelector the accessSelector to set
	 */
	public void setAccessSelector(AccessSelector accessSelector) {
		this.accessSelector = accessSelector;
	}
	/**
	 * @return the accessParameters
	 */
	public AbstractDataType getAccessParameters() {
		return accessParameters;
	}
	/**
	 * @param accessParameters the accessParameters to set
	 */
	public void setAccessParameters(AbstractDataType accessParameters) {
		this.accessParameters = accessParameters;
	}

}
