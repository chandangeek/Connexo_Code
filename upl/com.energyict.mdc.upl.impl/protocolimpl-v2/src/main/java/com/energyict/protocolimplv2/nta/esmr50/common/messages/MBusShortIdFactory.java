package com.energyict.protocolimplv2.nta.esmr50.common.messages;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Created by iulian on 10/6/2016.
 */
public class MBusShortIdFactory {

    private String longId;

    private String serialNumber;
    private String manufacturer;
    private String version;
    private String deviceType;


    public MBusShortIdFactory(String mBusSerialNumber) {
        updateFromSerialNumber(mBusSerialNumber);
    }


    public MBusShortIdFactory(byte[] imageFileAddressField){
        if (imageFileAddressField.length<8){
            throw new IndexOutOfBoundsException("Cannot decode an address field less than 8 bytes: "+ProtocolTools.getHexStringFromBytes(imageFileAddressField));
        }
        this.manufacturer = decodeManufacturer(ProtocolTools.getSubArray(imageFileAddressField, 0, 2));

        byte[] id = ProtocolTools.getSubArray(imageFileAddressField, 2, 6);
        this.serialNumber = ProtocolTools.getHexStringFromBytes(id,"");

        byte[] ver = ProtocolTools.getSubArray(imageFileAddressField, 6, 7);
        this.version = String.valueOf(ver[0]);
        this.version =  ProtocolTools.addPadding(this.version, '0' ,3, false);

        byte[] type = ProtocolTools.getSubArray(imageFileAddressField, 7, 8);
        this.deviceType = String.valueOf(type[0]);
        this.deviceType = ProtocolTools.addPadding(this.deviceType, '0', 2, false);

        this.longId = getManufacturer() + getSerialNumber(8) + getVersion() + getDeviceType();
    }

    private String decodeManufacturer(byte[] man) {
        if (man.length<2){
            // this is wrong!
            return ProtocolTools.getAsciiFromBytes(man);
        }
        int manuacturerId = (man[1]&0xff)*0x100 + (man[0]&0xff);

        StringBuilder sb = new StringBuilder(3);
        for (int i=0; i<3; i++){
            char c = (char)(manuacturerId & 0x1F);
            c += 64;
            sb.append(c);

            manuacturerId = manuacturerId >> 5;
        }
        return sb.reverse().toString();
    }

    public byte[] getShortIdForKeyChange(){
        return getSerialMnfVerType();
    }


    public byte[] getShortIdForFwUpgrade(){
        byte[] id = getSerialNumberAsHexReversed();
        byte[] mft = getManufacturerLsbMsb();
        byte[] ver = ProtocolTools.getBytesFromInt(Integer.valueOf(getVersion()), 1);
        byte[] dt = ProtocolTools.getBytesFromInt(Integer.valueOf(getDeviceType()), 1);
        return ProtocolTools.concatByteArrays(id, mft, ver, dt);
    }


    public byte[] getSerialMnfVerType() {
        byte[] id = getSerialNumberAsHexReversed();
        byte[] mft = getManufacturerLsbMsb();
        byte[] ver = ProtocolTools.getBytesFromInt(Integer.valueOf(getVersion()), 1);
        byte[] dt = ProtocolTools.getBytesFromInt(Integer.valueOf(getDeviceType()), 1);
        return ProtocolTools.concatByteArrays(id, mft, ver, dt);
    }

    public byte[] getMnfSerialVerType() {
        byte[] id = getSerialNumberAsHexReversed();
        byte[] mft = getManufacturerLsbMsb();
        byte[] ver = ProtocolTools.getBytesFromInt(Integer.valueOf(getVersion()), 1);
        byte[] dt = ProtocolTools.getBytesFromInt(Integer.valueOf(getDeviceType()), 1);
        return ProtocolTools.concatByteArrays(mft, id, ver, dt);
    }


    public byte[] getSerialNumberAsHex(){
        byte[] data = ProtocolTools.getBytesFromHexString(getSerialNumber(), 2);
        return data;
    }

    public byte[] getSerialNumberAsHexReversed(){
        return ProtocolTools.reverseByteArray(getSerialNumberAsHex());
    }

    public byte[] getManufacturerLsbMsb(){
        byte[] mnf = getManufacturer().getBytes();
        int value = (mnf[0]-64)*32*32 +
                    (mnf[1]-64)*32 +
                    (mnf[2]-64) ;
        byte[] ret = ProtocolTools.getBytesFromInt(value, 2);
        return ProtocolTools.reverseByteArray(ret);
    }
    public String getLongId() {
        return longId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }


    public String getSerialNumber(int length) {
        return serialNumber.substring(0, length);
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getVersion() {
        return version;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void updateFromSerialNumber(String mBusSerialNumber) {
        longId = mBusSerialNumber;

        if (mBusSerialNumber.length()!=16){
            throw new ExceptionInInitializerError("MBus serial number length not as expected (16): "+mBusSerialNumber);
        }

        this.manufacturer = longId.substring(0,3);
        this.serialNumber = longId.substring(3,3+8);
        this.version = longId.substring(11, 11+3);
        this.deviceType = longId.substring(14, 14+2);
    }

    public byte[] formatForAddressField() {
        return getMnfSerialVerType();
    }
}
