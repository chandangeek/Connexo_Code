/*
 * SelfReadDataRecord.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class SelfReadDataRecord {
    
    
    private int reasonForTrigger; // UNSIGNED16,
    private int year; // BYTE,
    private int month; // BYTE,
    private int day; // BYTE,
    private int hour; // BYTE,
    private int minute; // BYTE,
    private int second; // BYTE,
    private List freezeRegisterDatas; // FREEZE_REG(numRegisterEntries),
    private int checksum; // UNSIGNED16.
    private Date date;
    /**
     * Creates a new instance of SelfReadDataRecord
     */
    public SelfReadDataRecord(byte[] data,int offset,TimeZone timeZone,SelfReadRegisterConfiguration selfReadRegisterConfiguration) throws IOException {
        setReasonForTrigger(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setYear(ProtocolUtils.getInt(data,offset++, 1));
        setMonth(ProtocolUtils.getInt(data,offset++, 1));
        setDay(ProtocolUtils.getInt(data,offset++, 1));
        setHour(ProtocolUtils.getInt(data,offset++, 1));
        setMinute(ProtocolUtils.getInt(data,offset++, 1));
        setSecond(ProtocolUtils.getInt(data,offset++, 1));
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.set(Calendar.YEAR, getYear());
        cal.set(Calendar.MONTH, getMonth());
        cal.set(Calendar.DAY_OF_MONTH, getDay());
        cal.set(Calendar.HOUR_OF_DAY, getHour());
        cal.set(Calendar.MINUTE, getMinute());
        cal.set(Calendar.SECOND, getSecond());
        setDate(cal.getTime());
        
        for (int i=0;i<selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes().length;i++) {
            
            SelfReadRegisterConfigurationType srct = selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes()[i];
            
            setFreezeRegisterDatas(new ArrayList());
            if (srct.isSelfReadDemandRegisterType()) {
                getFreezeRegisterDatas().add(new SelfReadDemandRegister(data,offset, timeZone));
                offset+=SelfReadDemandRegister.size();
            } else if (srct.isSelfReadEnergyRegisterType()) {
                getFreezeRegisterDatas().add(new SelfReadEnergyRegister(data,offset));
                offset+=SelfReadEnergyRegister.size();
            } else throw new IOException("SelfReadDataRecord, size, invalid selfReadRegisterConfigurationType "+srct);
        } // for (int i=0;i<selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes().length;i++)
        
        
        setChecksum(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadDataRecord:\n");
        strBuff.append("   date="+getDate()+"\n");
        strBuff.append("   reasonForTrigger="+getReasonForTrigger()+"\n");
        for (int i=0;i<getFreezeRegisterDatas().size();i++)
            strBuff.append("   freezeRegisterData("+i+")="+getFreezeRegisterDatas().get(i)+"\n");
        strBuff.append("   checksum="+getChecksum()+"\n");
        return strBuff.toString();
    }
    
    static public int size(SelfReadRegisterConfiguration selfReadRegisterConfiguration) throws IOException {
        int size=0;
        for (int i=0;i<selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes().length;i++) {
            SelfReadRegisterConfigurationType srct = selfReadRegisterConfiguration.getSelfReadRegisterConfigurationTypes()[i];
            if (srct.isSelfReadDemandRegisterType()) {
                size+=(SelfReadDemandRegister.size());
            } else if (srct.isSelfReadEnergyRegisterType()) {
                size+=(SelfReadEnergyRegister.size());
            } throw new IOException("SelfReadDataRecord, size, invalid selfReadRegisterConfigurationType "+srct);
        }
        return size+10;
    }
    
    public int getReasonForTrigger() {
        return reasonForTrigger;
    }
    
    public void setReasonForTrigger(int reasonForTrigger) {
        this.reasonForTrigger = reasonForTrigger;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getDay() {
        return day;
    }
    
    public void setDay(int day) {
        this.day = day;
    }
    
    public int getHour() {
        return hour;
    }
    
    public void setHour(int hour) {
        this.hour = hour;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    public int getSecond() {
        return second;
    }
    
    public void setSecond(int second) {
        this.second = second;
    }
    
    
    
    public int getChecksum() {
        return checksum;
    }
    
    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    public List getFreezeRegisterDatas() {
        return freezeRegisterDatas;
    }

    public void setFreezeRegisterDatas(List freezeRegisterDatas) {
        this.freezeRegisterDatas = freezeRegisterDatas;
    }
    
    
    
}
