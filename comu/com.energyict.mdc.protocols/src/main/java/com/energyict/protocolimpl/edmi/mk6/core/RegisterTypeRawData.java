/*
 * RegisterTypeRawData.java
 *
 * Created on 22 maart 2006, 10:58
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

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
