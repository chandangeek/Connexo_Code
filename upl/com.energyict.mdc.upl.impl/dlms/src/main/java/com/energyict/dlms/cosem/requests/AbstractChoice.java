package com.energyict.dlms.cosem.requests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author jme
 *
 */
public abstract class AbstractChoice implements Choice {

	public static final byte INVALID_CHOICE_NUMBER = -1;

	private Field choiceObject;

	public byte[] toByteArray() {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try {
			bytes.write(new byte[] {getChoiceNumber()});
			bytes.write(choiceObject.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes.toByteArray();
	}

	public Field getChoiceObject() {
		return choiceObject;
	}

	protected void setChoiceObject(Field choiceObject) {
		this.choiceObject = choiceObject;
	}

}
