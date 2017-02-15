/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BasePageDescriptor.java
 *
 * Created on 13 september 2006, 9:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol;

/**
 *
 * @author Koen
 */
public class BasePageDescriptor {

    private int baseAddress;
    private int length;
    private byte[] data;


    /** Creates a new instance of BasePageDescriptor */
    public BasePageDescriptor(int baseAddress,int length) {
        this.setBaseAddress(baseAddress);
        this.setLength(length);
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }



    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
