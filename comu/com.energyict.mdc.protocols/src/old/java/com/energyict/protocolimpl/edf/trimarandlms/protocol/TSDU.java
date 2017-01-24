/*
 * DSDU.java
 *
 * Created on 13 februari 2007, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

/**
 *
 * @author Koen
 */
public class TSDU {

    byte[] data;

    /** Creates a new instance of DSDU */
    public TSDU() {
    }

    public void init(byte[] data) {
        setData(data);
    }



    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
