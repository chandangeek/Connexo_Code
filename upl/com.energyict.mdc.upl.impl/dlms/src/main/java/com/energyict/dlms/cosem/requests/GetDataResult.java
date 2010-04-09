package com.energyict.dlms.cosem.requests;

import com.energyict.dlms.axrdencoding.AbstractDataType;


/**
 * @author jme
 *
 */
public class GetDataResult extends AbstractChoice {

	private static final int	DATA_ACCES_RESULT_ID	= 1;
	private static final int	DATA_ID					= 0;

	public byte getChoiceNumber() {
		if (getChoiceObject() instanceof AbstractDataType) {
			return DATA_ID;
		} else if (getChoiceObject() instanceof DataAccessResult) {
			return DATA_ACCES_RESULT_ID;
		} else {
			return INVALID_CHOICE_NUMBER;
		}
	}

}
