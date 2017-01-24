/*
 * ViewInformation.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class ViewInformation extends AbstractDataDefinition {


    private int currentViewID; // VIEW_ID,
    /*
    view ids
    1: 89
    2: 90
    3: 91
    4: 92
    5: 93
    6: 94
    7: 95
    8: 96
    9: 97
    10: 98
     */

    private int currentObjectIndex; // unsigned16,
    private int numberOfObjects; // unsigned16,
    private ViewableFileId currentObjectID; // VIEWABLE_FILE_ID, 2 bytes

    private int currentObjectType; // VIEWABLE_FILE_OBJECT_TYPE, 2 bytes
    /*
    OBJTYPE_MASSMEMORY 1
    OBJTYPE_HARMONICS 2
    OBJTYPE_VOLTAGEQUALITY 3
    OBJTYPE_EXTENDED_MASSMEMORY 6
    OBJTYPE_EXTENDED_EVENTLOG, 7
     */

    private long recordCapacity; // unsigned32,
    private long currentRecordNum; // unsigned32,
    private long lastRecordNumWritten; // unsigned32,
    private long firstRecordNumPresent; // unsigned32,
    private long bytesInRecord; // unsigned32,
    private Date currentRecordDateTime; // EXTENDED_DATE_TIME,
    private Date lastWrittenRecordDateTime; // EXTENDED_DATE_TIME,
    private Date firstRecordDateTime; // EXTENDED_DATE_TIME,
    private int[] restrictions = new int[12]; // restrictions array(MAX_RESTRICTION_BYTES=12)of unsigned8,
    private long recsPerRead; // unsigned32

    /**
     * Creates a new instance of ViewInformation
     */
    public ViewInformation(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ViewInformation:\n");
        strBuff.append("   bytesInRecord="+getBytesInRecord()+"\n");
        strBuff.append("   currentObjectID="+getCurrentObjectID()+"\n");
        strBuff.append("   currentObjectIndex="+getCurrentObjectIndex()+"\n");
        strBuff.append("   currentObjectType="+getCurrentObjectType()+"\n");
        strBuff.append("   currentRecordDateTime="+getCurrentRecordDateTime()+"\n");
        strBuff.append("   currentRecordNum="+getCurrentRecordNum()+"\n");
        strBuff.append("   currentViewID="+getCurrentViewID()+"\n");
        strBuff.append("   firstRecordDateTime="+getFirstRecordDateTime()+"\n");
        strBuff.append("   firstRecordNumPresent="+getFirstRecordNumPresent()+"\n");
        strBuff.append("   lastRecordNumWritten="+getLastRecordNumWritten()+"\n");
        strBuff.append("   lastWrittenRecordDateTime="+getLastWrittenRecordDateTime()+"\n");
        strBuff.append("   numberOfObjects="+getNumberOfObjects()+"\n");
        strBuff.append("   recordCapacity="+getRecordCapacity()+"\n");
        strBuff.append("   recsPerRead="+getRecsPerRead()+"\n");
        for (int i=0;i<getRestrictions().length;i++) {
            strBuff.append("       restrictions["+i+"]="+getRestrictions()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x0042; // 66 DLMS_VIEW_INFO
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;

        setCurrentViewID(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setCurrentObjectIndex(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setNumberOfObjects(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        currentObjectID = ViewableFileId.findViewableFileId(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setCurrentObjectType(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setRecordCapacity(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setCurrentRecordNum(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setLastRecordNumWritten(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setFirstRecordNumPresent(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setBytesInRecord(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
        setCurrentRecordDateTime(Utils.getDateFromDateTimeExtended(data,offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setLastWrittenRecordDateTime(Utils.getDateFromDateTimeExtended(data,offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setFirstRecordDateTime(Utils.getDateFromDateTimeExtended(data,offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        for (int i=0;i<getRestrictions().length;i++)
            getRestrictions()[i]=ProtocolUtils.getInt(data,offset++,1);
        setRecsPerRead(ProtocolUtils.getInt(data,offset,4));
        offset+=4;
    }

    public int getCurrentViewID() {
        return currentViewID;
    }

    public void setCurrentViewID(int currentViewID) {
        this.currentViewID = currentViewID;
    }

    public int getCurrentObjectIndex() {
        return currentObjectIndex;
    }

    public void setCurrentObjectIndex(int currentObjectIndex) {
        this.currentObjectIndex = currentObjectIndex;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public void setNumberOfObjects(int numberOfObjects) {
        this.numberOfObjects = numberOfObjects;
    }

    public ViewableFileId getCurrentObjectID() {
        return currentObjectID;
    }

    public void setCurrentObjectID(ViewableFileId currentObjectID) {
        this.currentObjectID = currentObjectID;
    }

    public int getCurrentObjectType() {
        return currentObjectType;
    }

    public void setCurrentObjectType(int currentObjectType) {
        this.currentObjectType = currentObjectType;
    }

    public long getRecordCapacity() {
        return recordCapacity;
    }

    public void setRecordCapacity(long recordCapacity) {
        this.recordCapacity = recordCapacity;
    }

    public long getCurrentRecordNum() {
        return currentRecordNum;
    }

    public void setCurrentRecordNum(long currentRecordNum) {
        this.currentRecordNum = currentRecordNum;
    }

    public long getLastRecordNumWritten() {
        return lastRecordNumWritten;
    }

    public void setLastRecordNumWritten(long lastRecordNumWritten) {
        this.lastRecordNumWritten = lastRecordNumWritten;
    }

    public long getFirstRecordNumPresent() {
        return firstRecordNumPresent;
    }

    public void setFirstRecordNumPresent(long firstRecordNumPresent) {
        this.firstRecordNumPresent = firstRecordNumPresent;
    }

    public long getBytesInRecord() {
        return bytesInRecord;
    }

    public void setBytesInRecord(long bytesInRecord) {
        this.bytesInRecord = bytesInRecord;
    }

    public Date getCurrentRecordDateTime() {
        return currentRecordDateTime;
    }

    public void setCurrentRecordDateTime(Date currentRecordDateTime) {
        this.currentRecordDateTime = currentRecordDateTime;
    }

    public Date getLastWrittenRecordDateTime() {
        return lastWrittenRecordDateTime;
    }

    public void setLastWrittenRecordDateTime(Date lastWrittenRecordDateTime) {
        this.lastWrittenRecordDateTime = lastWrittenRecordDateTime;
    }

    public Date getFirstRecordDateTime() {
        return firstRecordDateTime;
    }

    public void setFirstRecordDateTime(Date firstRecordDateTime) {
        this.firstRecordDateTime = firstRecordDateTime;
    }

    public int[] getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(int[] restrictions) {
        this.restrictions = restrictions;
    }

    public long getRecsPerRead() {
        return recsPerRead;
    }

    public void setRecsPerRead(long recsPerRead) {
        this.recsPerRead = recsPerRead;
    }

}
