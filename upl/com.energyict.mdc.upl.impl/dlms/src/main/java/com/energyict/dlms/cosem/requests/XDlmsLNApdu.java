package com.energyict.dlms.cosem.requests;

/**
 * @author jme
 *
 */
public class XDlmsLNApdu extends AbstractChoice {

	private static final byte	GET_REQUEST_ID	= (byte) 192;

	public XDlmsLNApdu() {
		setChoiceObject(new GetRequest());
	}

	public byte getChoiceNumber() {
		if (getChoiceObject() instanceof GetRequest) {
			return GET_REQUEST_ID;
		} else {
			return INVALID_CHOICE_NUMBER;
		}
	}

}
