/*
 * TableTemplate.java
 *
 * Created on 28 oktober 2005, 17:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class UtilityInformationTable extends AbstractTable {

    private String ownerName; // 20 bytes
    private String utilityDivision; // 20 bytes

    private String servicePointId; // 10 bytes BCD or 20 bytes Char
    private String elecAddr; // 10 bytes BCD or 20 bytes Char
    private String deviceId; // 10 bytes BCD or 20 bytes Char
    private String utilSerNr; // 10 bytes BCD or 20 bytes Char
    private String customerId; // 10 bytes BCD or 20 bytes Char

    private byte[] coordinate1; // 10 bytes of UINT8
    private byte[] coordinate2; // 10 bytes of UINT8
    private byte[] coordinate3; // 10 bytes of UINT8
    private String tariffId; // 8 bytes
    private String ex1SwVendor; // 4 bytes
    private int ex1SwVersionNumber; // 1 byte UINT8
    private int ex1SwRevisionNumber; // 1 byte UINT8
    private String ex2SwVendor; // 4 bytes
    private int ex2SwVersionNumber; // 1 byte UINT8
    private int ex2SwRevisionNumber; // 1 byte UINT8
    private String programmerName; // 10 bytes
    private String miscId; // 30 bytes


    /** Creates a new instance of TableTemplate */
    public UtilityInformationTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(6));
    }



    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("UtilityInformationTable:\n");
        strBuff.append("   coordinate1="+getCoordinate1()+"\n");
        strBuff.append("   coordinate2="+getCoordinate2()+"\n");
        strBuff.append("   coordinate3="+getCoordinate3()+"\n");
        strBuff.append("   servicePointId="+getServicePointId()+"\n");
        strBuff.append("   elecAddr="+getElecAddr()+"\n");
        strBuff.append("   deviceId="+getDeviceId()+"\n");
        strBuff.append("   utilSerNr="+getUtilSerNr()+"\n");
        strBuff.append("   customerId="+getCustomerId()+"\n");
        strBuff.append("   ex1SwRevisionNumber="+getEx1SwRevisionNumber()+"\n");
        strBuff.append("   ex1SwVendor="+getEx1SwVendor()+"\n");
        strBuff.append("   ex1SwVersionNumber="+getEx1SwVersionNumber()+"\n");
        strBuff.append("   ex2SwRevisionNumber="+getEx2SwRevisionNumber()+"\n");
        strBuff.append("   ex2SwVendor="+getEx2SwVendor()+"\n");
        strBuff.append("   ex2SwVersionNumber="+getEx2SwVersionNumber()+"\n");
        strBuff.append("   miscId="+getMiscId()+"\n");
        strBuff.append("   ownerName="+getOwnerName()+"\n");
        strBuff.append("   programmerName="+getProgrammerName()+"\n");
        strBuff.append("   tariffId="+getTariffId()+"\n");
        strBuff.append("   utilityDivision="+getUtilityDivision()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        ActualRegisterTable art = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualRegisterTable();
        ActualTimeAndTOUTable atatt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualTimeAndTOUTable();
        ConfigurationTable cfgt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        ActualLogTable alt = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualLogTable();

        int offset=0;


        setOwnerName(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
        setUtilityDivision(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
        if (cfgt.getIdForm() == 1) {
            setServicePointId(new String(ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(tableData, offset, 10)))); offset+=10;
            setElecAddr(new String(ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(tableData, offset, 10)))); offset+=10;
            setDeviceId(new String(ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(tableData, offset, 10)))); offset+=10;
            setUtilSerNr(new String(ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(tableData, offset, 10)))); offset+=10;
            setCustomerId(new String(ProtocolUtils.convertAscii2Binary(ProtocolUtils.getSubArray2(tableData, offset, 10)))); offset+=10;
        }
        else {
            setServicePointId(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
            setElecAddr(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
            setDeviceId(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
            setUtilSerNr(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
            setCustomerId(new String(ProtocolUtils.getSubArray2(tableData, offset, 20))); offset+=20;
        }

        setCoordinate1(new byte[10]);
        setCoordinate1(ProtocolUtils.getSubArray2(tableData, offset, 10)); offset+=10;
        setCoordinate2(new byte[10]);
        setCoordinate2(ProtocolUtils.getSubArray2(tableData, offset, 10)); offset+=10;
        setCoordinate3(new byte[10]);
        setCoordinate3(ProtocolUtils.getSubArray2(tableData, offset, 10)); offset+=10;
        setTariffId(new String(ProtocolUtils.getSubArray2(tableData, offset, 8))); offset+=8;
        setEx1SwVendor(new String(ProtocolUtils.getSubArray2(tableData, offset, 4))); offset+=4;
        int ex1SwVersionNumber = C12ParseUtils.getInt(tableData,offset++);
        int ex1SwRevisionNumber = C12ParseUtils.getInt(tableData,offset++);
        String ex2SwVendor = new String(ProtocolUtils.getSubArray2(tableData, offset, 4)); offset+=4;
        int ex2SwVersionNumber = C12ParseUtils.getInt(tableData,offset++);
        int ex2SwRevisionNumber = C12ParseUtils.getInt(tableData,offset++);
        String programmerName = new String(ProtocolUtils.getSubArray2(tableData, offset, 10)); offset+=10;
        String miscId = new String(ProtocolUtils.getSubArray2(tableData, offset, 30)); offset+=30;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getUtilityDivision() {
        return utilityDivision;
    }

    public void setUtilityDivision(String utilityDivision) {
        this.utilityDivision = utilityDivision;
    }

    public String getServicePointId() {
        return servicePointId;
    }

    public void setServicePointId(String servicePointId) {
        this.servicePointId = servicePointId;
    }

    public String getElecAddr() {
        return elecAddr;
    }

    public void setElecAddr(String elecAddr) {
        this.elecAddr = elecAddr;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUtilSerNr() {
        return utilSerNr;
    }

    public void setUtilSerNr(String utilSerNr) {
        this.utilSerNr = utilSerNr;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public byte[] getCoordinate1() {
        return coordinate1;
    }

    public void setCoordinate1(byte[] coordinate1) {
        this.coordinate1 = coordinate1;
    }

    public byte[] getCoordinate2() {
        return coordinate2;
    }

    public void setCoordinate2(byte[] coordinate2) {
        this.coordinate2 = coordinate2;
    }

    public byte[] getCoordinate3() {
        return coordinate3;
    }

    public void setCoordinate3(byte[] coordinate3) {
        this.coordinate3 = coordinate3;
    }

    public String getTariffId() {
        return tariffId;
    }

    public void setTariffId(String tariffId) {
        this.tariffId = tariffId;
    }

    public String getEx1SwVendor() {
        return ex1SwVendor;
    }

    public void setEx1SwVendor(String ex1SwVendor) {
        this.ex1SwVendor = ex1SwVendor;
    }

    public int getEx1SwVersionNumber() {
        return ex1SwVersionNumber;
    }

    public void setEx1SwVersionNumber(int ex1SwVersionNumber) {
        this.ex1SwVersionNumber = ex1SwVersionNumber;
    }

    public int getEx1SwRevisionNumber() {
        return ex1SwRevisionNumber;
    }

    public void setEx1SwRevisionNumber(int ex1SwRevisionNumber) {
        this.ex1SwRevisionNumber = ex1SwRevisionNumber;
    }

    public String getEx2SwVendor() {
        return ex2SwVendor;
    }

    public void setEx2SwVendor(String ex2SwVendor) {
        this.ex2SwVendor = ex2SwVendor;
    }

    public int getEx2SwVersionNumber() {
        return ex2SwVersionNumber;
    }

    public void setEx2SwVersionNumber(int ex2SwVersionNumber) {
        this.ex2SwVersionNumber = ex2SwVersionNumber;
    }

    public int getEx2SwRevisionNumber() {
        return ex2SwRevisionNumber;
    }

    public void setEx2SwRevisionNumber(int ex2SwRevisionNumber) {
        this.ex2SwRevisionNumber = ex2SwRevisionNumber;
    }

    public String getProgrammerName() {
        return programmerName;
    }

    public void setProgrammerName(String programmerName) {
        this.programmerName = programmerName;
    }

    public String getMiscId() {
        return miscId;
    }

    public void setMiscId(String miscId) {
        this.miscId = miscId;
    }
}
