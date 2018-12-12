package com.energyict.protocolimpl.edmi.common.core;

/**
 *
 * @author koen
 */
public class RegisterTypeRawData extends AbstractRegisterType {
    
    private byte[] value;
    
    /** Creates a new instance of RegisterTypeRawData */
    public RegisterTypeRawData(byte[] data) {
        this.value = data;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
    
}
