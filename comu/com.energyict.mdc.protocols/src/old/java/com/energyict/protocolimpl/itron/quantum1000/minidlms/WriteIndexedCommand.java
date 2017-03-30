/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Write.java
 *
 * Created on 4 december 2006, 14:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class WriteIndexedCommand extends AbstractWriteCommand {
    
    private int indexTag;
    private int index;
    
    /** Creates a new instance of WriteIndexedCommand */
    public WriteIndexedCommand(CommandFactory commandFactory) {
        super(commandFactory);
        setIndexTag(0x04);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("WriteIndexedCommand:\n");
        strBuff.append("   index="+getIndex()+"\n");
        strBuff.append("   indexTag="+getIndexTag()+"\n");
        return strBuff.toString();
    }
    
    protected int getAccessTag() {
        return 0x01;
    }
    
    protected byte[] prepareBuild() {
        byte[] data = new byte[2];
        data[0] = (byte)getIndexTag();
        data[1] = (byte)getIndex();
        return data;
    }

    public int getIndexTag() {
        return indexTag;
    }

    private void setIndexTag(int indexTag) {
        this.indexTag = indexTag;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    
}
