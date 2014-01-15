/*
 * InformationObject.java
 *
 * Created on 18 juni 2003, 14:51
 */

package com.energyict.protocolimpl.iec870;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class IEC870InformationObject {


    public static final int QOI_STATION_INTERROGATION=0x14; // 7.2.6.22
    public static final int QCC_GENERAL_REQUEST_COUNTER=0x05; // 7.2.6.23


    int address;
    byte[] data;

    ByteArrayOutputStream bos=null;

    /** Creates a new instance of InformationObject */
    public IEC870InformationObject(int address) {
        this.address = address;
        bos = new ByteArrayOutputStream();
        bos.reset();
    }
    /** Creates a new instance of InformationObject */
    public IEC870InformationObject() {
        this(-1);
    }

    public void addData(int val) {
        bos.write(val);
    }
    public void addData(byte[] val) throws IEC870ConnectionException {
        try {
            bos.write(val);
        }
        catch(IOException e) {
            throw new IEC870ConnectionException("IEC870ConnectionException, addData, "+e.getMessage());
        }
    }

    public int getAddress() {
        return address;
    }

    public byte[] getData() throws IEC870ConnectionException {
        buildData();
        return data;
    }

    public byte[] getObjData() {
        return bos.toByteArray();
    }

    public String toString() {
        StringBuffer strbuff = new StringBuffer();
        String name;
        try {
            name = AddressMap.getAddressMapping(address).getType();
        }
        catch(NotFoundException e) {
            name = e.getMessage();
        }


        if (address != -1)
            strbuff.append("address=0x"+Integer.toHexString(address)+" ("+name+")\r\n");
        strbuff.append("data: ");
        strbuff.append(ProtocolUtils.getResponseData(getObjData()));
        strbuff.append("\r\n");
        return strbuff.toString();
    }

    private void buildData() throws IEC870ConnectionException {
        try {
            if (address == -1) {
                data = new byte[getObjData().length];
                ProtocolUtils.arrayCopy(getObjData(),data, 0);
            }
            else {
                data = new byte[getObjData().length+2];
                ProtocolUtils.arrayCopy(getObjData(),data, 2);
                data[0] = (byte)(address & 0xFF);
                data[1] = (byte)((address>>8) & 0xFF);
            }
        }
        catch(IOException e) {
            throw new IEC870ConnectionException("IEC870InformationObject, buildData, "+e.getMessage());
        }
    }
}
