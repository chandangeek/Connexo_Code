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

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

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
    
//        public static void main(String[] args) {
//            System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new CIField72h()));
//        }     
    
    protected int getId() {
        return 0x72;
    }
    
    public String getDeviceSerialNumberSecundaryAddress() {
    	return identificationNumber+"_"+Integer.toHexString(manufacturerIdentification)+"_"+Integer.toHexString(version)+"_"+Integer.toHexString(deviceType.getId());
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CIField72h:\n");
        strBuff.append("   meter3LetterId="+meter3LetterId+"\n");
        strBuff.append("   accessNumber="+getAccessNumber()+"\n");
        strBuff.append("   deviceType="+getDeviceType()+" (0x"+Integer.toHexString(getDeviceType().getId())+")\n");
        strBuff.append("   identificationNumber="+getIdentificationNumber()+"\n");
        strBuff.append("   manufacturerIdentification="+getManufacturerIdentification()+" (0x"+Integer.toHexString(getManufacturerIdentification())+")\n");
        strBuff.append("   signatureField="+getSignatureField()+"\n");
        strBuff.append("   statusByte="+getStatusByte()+"\n");
        strBuff.append("   version="+getVersion()+"\n");
        strBuff.append("   dataRecords="+getDataRecords()+"\n");
        return strBuff.toString();
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
            DataRecord dataRecord = new DataRecord(data,offset,timeZone, Logger.getLogger(CIField72h.class.getName()));
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
    
    static public void main(String[] args) {
        try {
             //                            0           1          2          3           4          5        6          7          8           9          10         11          12        13         14          15         16          17       18          19          20
//            byte[] data = new byte[]{(byte)0x45,(byte)0x00,(byte)0x48,(byte)0x07,(byte)0x8F,(byte)0x19,(byte)0x15,(byte)0x03,(byte)0xF9,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0D,(byte)0x78,(byte)0x11,(byte)0x37,(byte)0x30,(byte)0x35,(byte)0x34,(byte)0x30,(byte)0x30,(byte)0x38,(byte)0x34,(byte)0x37,(byte)0x30,(byte)0x30,(byte)0x30,(byte)0x34,(byte)0x30,(byte)0x30,(byte)0x38,(byte)0x32,(byte)0x0C,(byte)0x13,(byte)0x20,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x89,(byte)0x40,(byte)0xFD,(byte)0x1A,(byte)0x01,(byte)0x00};
            
            byte[] data = new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,(byte)0x85,(byte)0x10,(byte)0x08,(byte)0xe7,(byte)0x5e,(byte)0xf0,(byte)0x51,(byte)0x85,(byte)0x20,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xc5,(byte)0x10,(byte)0x08,(byte)0x2c,(byte)0x1e,(byte)0x30,(byte)0x53,(byte)0xc5,(byte)0x20,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x16,(byte)0x09,(byte)0x6c,(byte)0x2f,(byte)0x43,(byte)0x45,(byte)0x16,(byte)0x21,(byte)0x94,(byte)0x80,(byte)0x44,(byte)0x05,(byte)0x1b,(byte)0x7c,(byte)0x1b,(byte)0x14,(byte)0x48,(byte)0x45,(byte)0x1b,(byte)0x37,(byte)0x29,(byte)0x59,(byte)0x49,(byte)0x05,(byte)0x20,(byte)0x73,(byte)0xba,(byte)0x97,(byte)0x45,(byte)0x45,(byte)0x20,(byte)0xfd,(byte)0x75,(byte)0xa4,(byte)0x45,(byte)0x05,(byte)0x2b,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x3e,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x53,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x5b,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x5f,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x63,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x6d,(byte)0x1d,(byte)0x01,(byte)0x01,(byte)0x11,(byte)0x01,(byte)0x70,(byte)0x01,(byte)0x01,(byte)0x74,(byte)0x03,(byte)0x01,(byte)0x78,(byte)0x00,(byte)0x01,(byte)0x79,(byte)0x00,(byte)0x01,(byte)0x7a,(byte)0x02}; //,(byte)0x14,(byte)0x16};
            CIField72h o = new CIField72h(TimeZone.getTimeZone("ECT"));
            o.parse(data);
            System.out.println(o);
            
            
            System.out.println(CIField72h.getManufacturerCode("___"));
            System.out.println(CIField72h.getManufacturer3Letter(32767));
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
    }
}
