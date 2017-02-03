/*
 * MultiTariffScheduleGeneralParameters.java
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
import java.util.Date;

/**
 *
 * @author Koen
 */
public class MultiTariffScheduleGeneralParameters extends AbstractDataDefinition {
    
    private long fileId; // UNSIGNED32,
    private Date currentScheduleExpirationDate; // TOU_DATE,
    private int numberRateSchedules; // UNSIGNED8,
    private int rateScheduleDisplayed; // UNSIGNED8,
    private String scheduleName; // OctetString(8),
    private long scheduleId; // UNSIGNED32,
    private int checksum; // UNSIGNED16,
    
    /**
     * Creates a new instance of MultiTariffScheduleGeneralParameters
     */
    public MultiTariffScheduleGeneralParameters(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MultiTariffScheduleGeneralParameters:\n");
        strBuff.append("   checksum="+getChecksum()+"\n");
        strBuff.append("   currentScheduleExpirationDate="+getCurrentScheduleExpirationDate()+"\n");
        strBuff.append("   fileId="+getFileId()+"\n");
        strBuff.append("   numberRateSchedules="+getNumberRateSchedules()+"\n");
        strBuff.append("   rateScheduleDisplayed="+getRateScheduleDisplayed()+"\n");
        strBuff.append("   scheduleId="+getScheduleId()+"\n");
        strBuff.append("   scheduleName="+getScheduleName()+"\n");
        return strBuff.toString();
    }
    
    protected int getVariableName() {
        return 130; // DLMS_TOU_SCHEDULE_GENERAL_PARAMETERS
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        
        setFileId(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setCurrentScheduleExpirationDate(Utils.getDateFromTOUDate(data,offset, getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getTOUDateSize();
        setNumberRateSchedules(ProtocolUtils.getInt(data,offset++, 1));
        setRateScheduleDisplayed(ProtocolUtils.getInt(data,offset++, 1));
        setScheduleName(new String(ProtocolUtils.getSubArray2(data, offset, 8)));
        offset+=8;
        setScheduleId(ProtocolUtils.getLong(data,offset, 4));
        offset+=4;
        setChecksum(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        
    }
    
    public long getFileId() {
        return fileId;
    }
    
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }
    
    public Date getCurrentScheduleExpirationDate() {
        return currentScheduleExpirationDate;
    }
    
    public void setCurrentScheduleExpirationDate(Date currentScheduleExpirationDate) {
        this.currentScheduleExpirationDate = currentScheduleExpirationDate;
    }
    
    public int getNumberRateSchedules() {
        return numberRateSchedules;
    }
    
    public void setNumberRateSchedules(int numberRateSchedules) {
        this.numberRateSchedules = numberRateSchedules;
    }
    
    public int getRateScheduleDisplayed() {
        return rateScheduleDisplayed;
    }
    
    public void setRateScheduleDisplayed(int rateScheduleDisplayed) {
        this.rateScheduleDisplayed = rateScheduleDisplayed;
    }
    
    public String getScheduleName() {
        return scheduleName;
    }
    
    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }
    
    public long getScheduleId() {
        return scheduleId;
    }
    
    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    public int getChecksum() {
        return checksum;
    }
    
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }
}
