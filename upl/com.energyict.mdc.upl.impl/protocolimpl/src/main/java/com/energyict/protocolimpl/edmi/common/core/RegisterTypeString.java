package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.protocol.ProtocolUtils;


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
