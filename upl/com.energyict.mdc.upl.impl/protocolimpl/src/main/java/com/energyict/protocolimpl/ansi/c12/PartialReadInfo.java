/*
 * PartialReadInfo.java
 *
 * Created on 9 november 2005, 11:07
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
public class PartialReadInfo {
    
    private int offset;
    private int count;
    
    /** Creates a new instance of PartialReadInfo */
    public PartialReadInfo(int offset, int count) {
        this.setOffset(offset);
        this.setCount(count);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
}
