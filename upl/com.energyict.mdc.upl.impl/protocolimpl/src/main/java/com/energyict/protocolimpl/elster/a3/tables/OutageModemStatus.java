/*
 * OutageModemStatus.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class OutageModemStatus extends AbstractTable { 
    /*
    Memory storage: Virtual table (stored in outage modem)
    Total table size: (bytes) 34 (stored in outage modem)
    Read access: 1
    Write access: N/A

    MT-89 can only be read when the outage modem is present. When the outage modem is not
    present the meter will respond with error code = 0x04 (Operation Not Possible) when a read
    request for MT-89 is received.
    */

    private String acctId; // 14 byte This is written by the meter when the outage modem is initialized. The meter uses the LS 14 bytes of CUSTOMER_ID in ST-6. 
    private String mtrSn; // 5 bytes This is written by the meter when the outage modem is initialized. The meter uses the LS 5 bytes of DEVICE_ID in ST-6. 
    private Date outageTime; // 3 bytes The time of the outage occurrence. hh,mm,ss 
    // SPARE      4       
    private int modemStatusFlags; // 1 byte Outage modem status flags 
                          // b0 1 = Modem carrier present 
                          // b1 1 = Telephone line off-hook condition 
                          // b2 1 = Telephone line intrusion detected 
                          // b3 1 = Modem battery voltage low 
                          // b4 1 = Class 28 (MT-88) checksum error 
                          // b5 1 = Self Test error 
                          // b6-7 0 (unused ) 
    private long sspec3; // 5 bytes      
                 // bytes 1-3: SSPEC 
                 // byte 4:      Group 
                 // byte 5:      Revision Number 
    private int modemManufacturYear; // 1 bytes Year outage modem was manufactured 
    private int modemManufacturWeek; // 1 bytes Week in Year outage modem was manufactured 
    
    /** Creates a new instance of OutageModemStatus */
    public OutageModemStatus(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(89,true));
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OutageModemStatus:\n");
        strBuff.append("   acctId="+getAcctId()+"\n");
        strBuff.append("   modemManufacturWeek="+getModemManufacturWeek()+"\n");
        strBuff.append("   modemManufacturYear="+getModemManufacturYear()+"\n");
        strBuff.append("   modemStatusFlags=0x"+Integer.toHexString(getModemStatusFlags())+"\n");
        strBuff.append("   mtrSn="+getMtrSn()+"\n");
        strBuff.append("   outageTime="+getOutageTime()+"\n");
        strBuff.append("   sspec3="+getSspec3()+"\n");
        return strBuff.toString();
    }    
    
//     public static void main(String[] args) {
//        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new OutageModemStatus(null)));
//     } 
    
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int timeFormat = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getTimeFormat();
        
        int offset = 0;
        setAcctId(new String(ProtocolUtils.getSubArray2(tableData,offset, 14))); offset+=14;
        setMtrSn(new String(ProtocolUtils.getSubArray2(tableData,offset, 5))); offset+=5;
        setOutageTime(C12ParseUtils.getDateFromTime(tableData, offset, timeFormat, getTableFactory().getC12ProtocolLink().getTimeZone(), dataOrder)); offset+=3;
        setModemStatusFlags(C12ParseUtils.getInt(tableData,offset++));
        setSspec3(C12ParseUtils.getLong(tableData, offset, 5, dataOrder)); offset+=5;
        setModemManufacturYear(C12ParseUtils.getInt(tableData,offset++));      
        setModemManufacturWeek(C12ParseUtils.getInt(tableData,offset++));      
    } 
    
    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public String getMtrSn() {
        return mtrSn;
    }

    public void setMtrSn(String mtrSn) {
        this.mtrSn = mtrSn;
    }

    public Date getOutageTime() {
        return outageTime;
    }

    public void setOutageTime(Date outageTime) {
        this.outageTime = outageTime;
    }

    public int getModemStatusFlags() {
        return modemStatusFlags;
    }

    public void setModemStatusFlags(int modemStatusFlags) {
        this.modemStatusFlags = modemStatusFlags;
    }

    public long getSspec3() {
        return sspec3;
    }

    public void setSspec3(long sspec3) {
        this.sspec3 = sspec3;
    }

    public int getModemManufacturYear() {
        return modemManufacturYear;
    }

    public void setModemManufacturYear(int modemManufacturYear) {
        this.modemManufacturYear = modemManufacturYear;
    }

    public int getModemManufacturWeek() {
        return modemManufacturWeek;
    }

    public void setModemManufacturWeek(int modemManufacturWeek) {
        this.modemManufacturWeek = modemManufacturWeek;
    }
    

}
