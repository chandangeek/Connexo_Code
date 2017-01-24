/*
 * RegisterTypeString.java
 *
 * Created on 21 maart 2006, 17:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import com.energyict.protocols.util.ProtocolUtils;


/**
 *
 * @author koen
 */
public class RegisterTypeString extends AbstractRegisterType {

	private String value;

	/** Creates a new instance of RegisterTypeString */
	public RegisterTypeString(byte[] data) {
		int i;
		for (i=0;i<data.length;i++) {
			if (data[i]==0) {
				break;
			}
		}
		this.value = new String(ProtocolUtils.getSubArray2(data,0, i==data.length?data.length-1:i));
	}

	public String getValue() {
		return value;
	}

	public String getString() {
		return getValue();
	}

	public void setValue(String value) {
		this.value = value;
	}



}
