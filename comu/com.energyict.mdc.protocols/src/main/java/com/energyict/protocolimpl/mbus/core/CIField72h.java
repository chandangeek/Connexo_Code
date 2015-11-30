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

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author kvds
 */
public class CIField72h extends AbstractCIField {

    private long identificationNumber;
    private int manufacturerIdentification;
    private String meter3LetterId;
    private int version;
    DeviceType deviceType;
    private int accessNumber;
    private int statusByte;
    private int signatureField;

    private List dataRecords;

    TimeZone timeZone;

    /** Creates a new instance of CIField72h */
    public CIField72h(TimeZone timeZone) {
        this.timeZone=timeZone;
    }

    protected int getId() {
        return 0x72;
    }

    public String getDeviceSerialNumberSecundaryAddress() {
    	return identificationNumber+"_"+Integer.toHexString(manufacturerIdentification)+"_"+Integer.toHexString(version)+"_"+Integer.toHexString(deviceType.getId());
    }

    public String toString() {
        return "CIField72h:\n" +
                "   meter3LetterId=" + meter3LetterId + "\n" +
                "   accessNumber=" + getAccessNumber() + "\n" +
                "   deviceType=" + getDeviceType() + " (0x" + Integer.toHexString(getDeviceType().getId()) + ")\n" +
                "   identificationNumber=" + getIdentificationNumber() + "\n" +
                "   manufacturerIdentification=" + getManufacturerIdentification() + " (0x" + Integer.toHexString(getManufacturerIdentification()) + ")\n" +
                "   signatureField=" + getSignatureField() + "\n" +
                "   statusByte=" + getStatusByte() + "\n" +
                "   version=" + getVersion() + "\n" +
                "   dataRecords=" + getDataRecords() + "\n";
    }

    public String header() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CIField72h:\n");
        strBuff.append("   meter3LetterId="+meter3LetterId+"\n");
        strBuff.append("   manufacturerIdentification="+getManufacturerIdentification()+"\n");
        strBuff.append("   version="+getVersion()+"\n");
        strBuff.append("   deviceType="+getDeviceType()+"\n");
        strBuff.append("   accessNumber="+getAccessNumber()+"\n");
        strBuff.append("   identificationNumber="+getIdentificationNumber()+"\n");
        strBuff.append("   statusByte="+getStatusByte()+"\n");
        strBuff.append("   signatureField="+getSignatureField()+"\n");
        return strBuff.toString();
    }

    protected void doParse(byte[] data) throws IOException {
        int offset=0;
        setIdentificationNumber(ParseUtils.getBCD2LongLE(data,offset,4));
        offset+=4;
        setManufacturerIdentification(ProtocolUtils.getIntLE(data,offset,2));
        offset+=2;
        setVersion(ProtocolUtils.getIntLE(data,offset++,1));
        setDeviceType(DeviceType.findDeviceType(ProtocolUtils.getIntLE(data,offset++,1)));
        setAccessNumber(ProtocolUtils.getIntLE(data,offset++,1));
        setStatusByte(ProtocolUtils.getIntLE(data,offset++,1));
        setSignatureField(ProtocolUtils.getIntLE(data,offset,2));
        offset+=2;

        setMeter3LetterId(getManufacturer3Letter(getManufacturerIdentification()));

        // build the data records
        setDataRecords(new ArrayList());
        while(offset<data.length) {
            DataRecord dataRecord = new DataRecord(data,offset,timeZone);
            getDataRecords().add(dataRecord);
            offset+=dataRecord.size();


            // KV 12112008 In case of NODATA!
            if (dataRecord.getDataRecordHeader().getDataInformationBlock().getDataInformationfield().getDataFieldCoding().isTYPE_NODATA())
            	offset++;

            // break when encountering manufacturer specific data structure...
            if (dataRecord.getDataRecordHeader().getDataInformationBlock().getDataInformationfield().getDataFieldCoding().isTYPE_SPECIALFUNCTIONS())
                break;
        }

    }

    public long getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(long identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public int getManufacturerIdentification() {
        return manufacturerIdentification;
    }

    public void setManufacturerIdentification(int manufacturerIdentification) {
        this.manufacturerIdentification = manufacturerIdentification;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public int getAccessNumber() {
        return accessNumber;
    }

    public void setAccessNumber(int accessNumber) {
        this.accessNumber = accessNumber;
    }

    public int getStatusByte() {
        return statusByte;
    }

    public void setStatusByte(int statusByte) {
        this.statusByte = statusByte;
    }

    public int getSignatureField() {
        return signatureField;
    }

    public void setSignatureField(int signatureField) {
        this.signatureField = signatureField;
    }

    public String getMeter3LetterId() {
        return meter3LetterId;
    }

    public void setMeter3LetterId(String meter3LetterId) {
        this.meter3LetterId = meter3LetterId;
    }

    public List getDataRecords() {
        return dataRecords;
    }

    public void setDataRecords(List dataRecords) {
        this.dataRecords = dataRecords;
    }

    static public String getManufacturer3Letter(int manufacturerCode) {
        char[] kars = new char[3];
        for (int i=0;i<3;i++) {
            kars[i] = (char)(((manufacturerCode>>(5*(2-i)))&0x1f) | 0x40);
        }
    	return new String(kars);
    }

    static public int getManufacturerCode(String id) {
        int val = ((int)id.charAt(0)-64)*32*32+((int)id.charAt(1)-64)*32+((int)id.charAt(2)-64);
        return val;
    }

}
