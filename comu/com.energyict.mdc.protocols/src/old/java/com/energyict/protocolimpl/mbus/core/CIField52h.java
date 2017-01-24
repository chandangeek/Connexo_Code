/*
 * CIField72h.java
 *
 * Created on 3 oktober 2007, 13:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class CIField52h extends AbstractCIField {

    /** Creates a new instance of CIField72h */
    public CIField52h() {
    }
    
    protected int getId() {
        return 0x52;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CIField52h:\n");
        return strBuff.toString();
    }    
    
    protected void doParse(byte[] data) throws IOException {
        int offset=0;
    }

    
}
