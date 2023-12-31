/*
 * ReadIndexedWithRange.java
 *
 * Created on 1 december 2006, 14:17
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
public class ReadIndexedWithRangeCommand extends AbstractReadCommand  {
    
    private int indexRangeTag;
    private int index;
    private int range;
    
    /** Creates a new instance of ReadIndexedCommand */
    public ReadIndexedWithRangeCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }    
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadIndexedWithRangeCommand:\n");
        strBuff.append("   index="+getIndex()+"\n");
        strBuff.append("   indexRangeTag="+getIndexRangeTag()+"\n");
        strBuff.append("   range="+getRange()+"\n");
        return strBuff.toString();
    }
    
    protected int getAccessTag() {
        return 0x01;
    }
    
    protected byte[] prepareBuild() {
        byte[] data = new byte[3];
        data[0] = (byte)getIndexRangeTag();
        data[1] = (byte)getIndex();
        data[2] = (byte)getRange();
        return data;
    }    
    
    public int getIndexRangeTag() {
        return indexRangeTag;
    }

    public void setIndexRangeTag(int indexRangeTag) {
        this.indexRangeTag = indexRangeTag;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
    
}
