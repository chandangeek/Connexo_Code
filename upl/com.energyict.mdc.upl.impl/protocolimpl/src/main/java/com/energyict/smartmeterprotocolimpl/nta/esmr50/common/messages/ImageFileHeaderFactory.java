package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by iulian on 10/6/2016.
 */
@Deprecated
public class ImageFileHeaderFactory {
    public static final int ACTIVATION_IMMEDIATE = 1;
    public static final int ACTIVATION_TIMED = 2;
    public static final int ACTIVATION_MASTER = 3;
    private static final String MBUS_DEV = "MBUS";


    private final byte[] imageData;
    private byte[] workingHeader;
    private byte[] imageMagicNumber;

    private byte[] usefullImage;

    private int index;
    private long headerVersion;
    private long headerLength;
    private long imageVersion;
    private long imageLength;
    private long securityLength;
    private long securityType;
    private long addressLength;
    private long addressType;
    private byte[] addressField;
    private long activationType;
    private MBusDateTypeI activationDate;
    private MBusShortIdFactory shortIdFactory;
    private Logger logger;



    public ImageFileHeaderFactory(byte[] imageData, Logger logger) {
        this.logger = logger;
        this.imageData = imageData;
        parseHeader();
        validateHeader();
    }

    private void parseHeader() {
        reset();
        imageMagicNumber = extractNext(4);
        headerVersion = extractNext1();
        headerLength = extractNext2();
        imageVersion = extractNext4();
        imageLength = extractNext4();
        securityLength = extractNext2();
        securityType = extractNext1();
        addressLength = extractNext1();
        addressType = extractNext1();
        addressField = extractNext(addressLength);
        activationType = extractNext1();
        activationDate = new MBusDateTypeI(extractNext(6));
        usefullImage = extractNext(imageData.length - index);

        this.shortIdFactory = new MBusShortIdFactory(addressField);
    }

    private void rebuildWorkingHeader(){
        workingHeader = ProtocolTools.getSubArray(imageMagicNumber, 0);
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get1(headerVersion));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get2(headerLength));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get4(imageVersion));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get4(imageLength));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get2(securityLength));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get1(securityType));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get1(addressLength));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get1(addressType));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, addressField);
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, get1(activationType));
        workingHeader = ProtocolTools.concatByteArrays(workingHeader, activationDate.getEncoded(isImmediateActivation()));
    }

    private boolean isImmediateActivation() {
        return ACTIVATION_IMMEDIATE == activationType;
    }

    private byte[] get1(long val) {
        byte[] b = new byte[1];
        b[0] = (byte) (val & 0xff);
        return b;
    }

    private byte[] get2(long val) {
        byte[] b = new byte[2];
        b[1] = (byte) ((val & 0xff00) >> 4);
        b[0] = (byte) (val & 0xff);
        return b;
    }

    private byte[] get4(long val) {
        byte[] b = new byte[4];
        b[3] = (byte) (((val & 0xff000000L) >> 24) & 0xff);
        b[2] = (byte) (((val & 0x00ff0000L) >> 16) & 0xff);
        b[1] = (byte) (((val & 0x0000ff00L) >> 8) & 0xff);
        b[0] = (byte)   (val & 0x000000ffL);
        return b;
    }


    public byte[] getUpdatedImageData(){
        rebuildWorkingHeader();
        return ProtocolTools.concatByteArrays(workingHeader, usefullImage);
    }

    public void validateHeader(){
        long expected = 27 + addressLength;
        if (headerLength != expected){
            getLogger().warning("Header length does not match the value received: "+expected+ "actual vs "+headerLength+" in header");
        }

        if (imageData.length != imageLength){
            getLogger().warning("Image data length does not match the value received: "+imageData.length+" actual vs "+imageLength+" in header");
        }

        if (addressLength != 8){
            getLogger().warning("Address field is not 8, will be impossible to decode it now: "+addressLength+" received");
        }
    }

    private byte extractNext1() {
        byte[] bytes = extractNext(1);
        return bytes[0];
    }

    private int extractNext2() {
        byte[] bytes = extractNext(2);
        int lo = bytes[0] & 0xff;
        int hi = bytes[1] & 0xff;
        return hi*0x100 + lo;
    }


    private long extractNext4() {
        long lo = extractNext2();
        long hi = extractNext2();
        long hi2 = (hi * 0x10000L) & 0xffff0000L;
        return hi2 + lo;
    }


    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("imageMagicNumber").append(":").append(imageMagicNumber).append(", ");
        sb.append("headerVersion").append(":").append(headerVersion).append(", ");
        sb.append("headerLength").append(":").append(headerLength).append(", ");
        sb.append("imageVersion").append(":").append(imageVersion).append(", ");
        sb.append("imageLength").append(":").append(imageLength).append(", ");
        sb.append("securityLength").append(":").append(securityLength).append(", ");
        sb.append("securityType").append(":").append(securityType).append(", ");
        sb.append("addressLength").append(":").append(addressLength).append(", ");
        sb.append("addressType").append(":").append(addressType).append(", ");
        sb.append("addressField").append(":").append(ProtocolTools.getHexStringFromBytes(addressField)).append(", ");
        sb.append("activationType").append(":").append(activationType).append(", ");
        sb.append("activationDate").append(":").append(activationDate.toString()).append("");

        return sb.toString();
    }

    public static long convert(byte[] bytes) {
        int x = 0;
        int i;
        for (i=0; i<bytes.length; i++){
            x = x << 8;
            x += bytes[i];
        }
        return x;
    }

    private void reset() {
        index = 0;
    }

    private byte[] extractNext(long len){
        byte[] data = ProtocolTools.getSubArray(imageData, index, index+ (int) len);
        index += len;
        return data;
    }

    public byte[] getUsefullImage() {
        return usefullImage;
    }

    public byte[] getImageMagicNumber() {
        return imageMagicNumber;
    }

    public long getHeaderVersion() {
        return headerVersion;
    }

    public long getHeaderLength() {
        return headerLength;
    }

    public long getImageVersion() {
        return imageVersion;
    }

    public long getImageLength() {
        return imageLength;
    }

    public long getSecurityLength() {
        return securityLength;
    }

    public long getSecurityType() {
        return securityType;
    }

    public long getAddressLength() {
        return addressLength;
    }

    public long getAddressType() {
        return addressType;
    }

    public byte[] getAddressField() {
        return addressField;
    }

    public long getActivationType() {
        return activationType;
    }

    public long getActivationDate() {
        return activationDate.getDate().getTime();
    }

    public MBusShortIdFactory getShortIdFactory(){
        return shortIdFactory;
    }


    public void setImageVersion(byte[] imageVersion) {
        getLogger().finest(" - setting imageVersion [bytes]:"+ProtocolTools.getHexStringFromBytes(imageVersion));
        this.imageVersion = ProtocolTools.getIntFromBytes(imageVersion);//todo check if int is sufficient, long was used before
        getLogger().finest("  > new imageVersion [long]:"+this.imageVersion);
        rebuildWorkingHeader();
    }

    public void setAddressField(String serialNumber) {
        getLogger().finest(" - setting addressField: "+ serialNumber);
        shortIdFactory.updateFromSerialNumber(serialNumber);
        this.addressField = shortIdFactory.formatForAddressField();
        getLogger().finest("  > new encoded addressField: "+ProtocolTools.getHexStringFromBytes(this.addressField));
        rebuildWorkingHeader();
    }

    public void setActivationType(int activationType) {
        getLogger().finest(" - setting activationType: "+ activationType);
        this.activationType = activationType;
        rebuildWorkingHeader();
    }

    public void setActivationDate(Calendar activationDateCalendar) {
        if (activationDateCalendar!=null) {
            Date activationDate = activationDateCalendar.getTime();
            getLogger().finer(" - setting activationDate: " + activationDate.toString());
            this.activationDate.update(activationDate);
            getLogger().finest(" > new encoded activationDate: " + ProtocolTools.getHexStringFromBytes(this.activationDate.getEncoded()));
        } else {
            getLogger().finer(" - setting immediate activationDate ");
            this.activationDate.update(new Date());
            this.activationType = ACTIVATION_IMMEDIATE; //safety set
        }
        rebuildWorkingHeader();
    }

    public Logger getLogger() {
        return logger;
    }

    public String getImageIdentifier(byte [] crc) {
        return getShortIdFactory().getManufacturer()+
                MBUS_DEV+
                ProtocolTools.getHexStringFromBytes(getShortIdFactory().getSerialMnfVerType()).replace("$","")+
                ProtocolTools.getHexStringFromBytes(crc).replace("$","");
    }
}
