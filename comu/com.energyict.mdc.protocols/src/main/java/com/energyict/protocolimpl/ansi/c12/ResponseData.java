/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ResponseData.java
 *
 * Created on 17 oktober 2005, 10:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

/**
 *
 * @author Koen
 */
public class ResponseData {

    private byte[] data;
    
    
    /** Creates a new instance of RequestInfo */
    public ResponseData() {
        this(null);
    }
    public ResponseData(byte[] data) {
        this.data=data;
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
}
