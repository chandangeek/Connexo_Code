/*
 * ViewConfigType.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under 
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ViewConfigType {
    
    private ViewableFileId viewableFileID; // 'VIEWABLE_FILE_ID',
    private int[] restrictions = new int[12]; // array('MAX_RESTRICTION_BYTES=12')of unsigned8,
    private long currentRecord; // unsigned32,
    private long recsPerRead; // unsigned32
            
    /**
     * Creates a new instance of ViewConfigType 
     */
    public ViewConfigType(byte[] data,int offset) throws IOException {
        setViewableFileID(ViewableFileId.findViewableFileId(ProtocolUtils.getInt(data,offset,2)));
        offset+=2;
        for (int i=0;i<getRestrictions().length;i++)
            getRestrictions()[i]=ProtocolUtils.getInt(data,offset++,1);
        setCurrentRecord(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
        setRecsPerRead(ProtocolUtils.getLong(data,offset,4));
        offset+=4;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ViewConfigType:\n");
        strBuff.append("   currentRecord="+getCurrentRecord()+"\n");
        strBuff.append("   recsPerRead="+getRecsPerRead()+"\n");
        for (int i=0;i<getRestrictions().length;i++) {
            strBuff.append("       restrictions["+i+"]="+getRestrictions()[i]+"\n");
        }
        strBuff.append("   viewableFileID="+getViewableFileID()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 22;
    }
    
    public ViewableFileId getViewableFileID() {
        return viewableFileID;
    }

    public void setViewableFileID(ViewableFileId viewableFileID) {
        this.viewableFileID = viewableFileID;
    }

    public int[] getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(int[] restrictions) {
        this.restrictions = restrictions;
    }

    public long getCurrentRecord() {
        return currentRecord;
    }

    public void setCurrentRecord(long currentRecord) {
        this.currentRecord = currentRecord;
    }

    public long getRecsPerRead() {
        return recsPerRead;
    }

    public void setRecsPerRead(long recsPerRead) {
        this.recsPerRead = recsPerRead;
    }
}
