package com.energyict.dlms.cosem.requests;

/**
 * @author jme
 */
public class GetRequest extends AbstractChoice {

	private static final int	GETREQUEST_WITHLIST_ID	= 3;
	private static final int	GETREQUEST_NEXT_ID		= 2;
	private static final int	GETREQUEST_NORMAL_ID	= 1;

	public GetRequest() {
		setChoiceObject(new GetRequestWithList());
	}

	public byte getChoiceNumber() {
		if (getChoiceObject() instanceof GetRequestNormal) {
			return GETREQUEST_NORMAL_ID;
		} else if (getChoiceObject() instanceof GetRequestNext) {
			return GETREQUEST_NEXT_ID;
		} else if (getChoiceObject() instanceof GetRequestWithList) {
			return GETREQUEST_WITHLIST_ID;
		} else {
			return INVALID_CHOICE_NUMBER;
		}
	}

}
