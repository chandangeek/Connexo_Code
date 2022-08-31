package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.header;


import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;

import java.util.Arrays;

public class  TelegramHeader {

    private TelegramField lField;
    private TelegramField cField;
    private TelegramField mField;
    private TelegramField aField;
    private TelegramField crcField;
    public static int headerLengthCRC = 11;
    public static int headerLengthNoCRC = 9;

    public TelegramHeader() {

    }

    public void createTelegramHeader(String header) {
        this.createTelegramHeader(header.split(" "));
    }

    public void createTelegramHeader(String[] header) {
        this.setLField(header[0]);
        this.setCField(header[1]);
        this.setMField(Arrays.copyOfRange(header, 2, 4));
        this.setAField(Arrays.copyOfRange(header, 4, 10));
        if(header.length == TelegramHeader.headerLengthCRC) {
            this.setCRCField(Arrays.copyOfRange(header, 10, 11));
        }
    }

    public String getSerialNr() {
        try {
            String serialNr = this.getaField().getFieldParts().get(3) + this.getaField().getFieldParts().get(2) +
                    this.getaField().getFieldParts().get(1) + this.getaField().getFieldParts().get(0);
            return serialNr;
        } catch (Exception ex) {
            return null;
        }
    }

    public TelegramField getlField() {
        return lField;
    }

    public void setlField(TelegramField lField) {
        this.lField = lField;
    }

    public TelegramField getcField() {
        return cField;
    }

    public void setcField(TelegramField cField) {
        this.cField = cField;
    }

    public TelegramField getmField() {
        return mField;
    }

    public void setmField(TelegramField mField) {
        this.mField = mField;
    }

    public TelegramField getaField() {
        return aField;
    }

    public void setaField(TelegramField aField) {
        this.aField = aField;
    }

    public TelegramField getCrcField() {
        return crcField;
    }

    public void setCrcField(TelegramField crcField) {
        this.crcField = crcField;
    }

    public void setLField(String lField) {
        this.lField = new TelegramField();
        this.lField.addFieldPart(lField);
    }

    public void setCField(String cField) {
        this.cField = new TelegramField();
        this.cField.addFieldPart(cField);
    }

    public void setMField(String[] mField) {
        this.mField = new TelegramField();
        this.mField.addFieldParts(mField);
    }

    public void setAField(String[] aField) {
        this.aField = new TelegramField();
        this.aField.addFieldParts(aField);
    }

    public void setCRCField(String[] crcField) {
        this.crcField = new TelegramField();
        this.crcField.addFieldParts(crcField);
    }

    public byte[] getAESCBCInitVectorPart() {
        byte[] aesCbcInitVectorPart = new byte[8];
        byte[] mFieldByteArr = this.mField.getFieldAsByteArray();
        byte[] aFieldByteArr = this.aField.getFieldAsByteArray();

        System.arraycopy(mFieldByteArr, 0, aesCbcInitVectorPart, 0, mFieldByteArr.length);
        System.arraycopy(aFieldByteArr, 0, aesCbcInitVectorPart, mFieldByteArr.length, aFieldByteArr.length);
        return aesCbcInitVectorPart;
    }

    public void debugOutput() {
        if(this.lField != null) {
            System.out.println("Length of Telegram: " + this.lField.getFieldParts().get(0));
        }
        if(this.cField != null) {
            System.out.println("C-Field (mode): " + this.cField.getFieldParts().get(0));
        }
        if(this.mField != null) {
            String manufacturer =  this.mField.getFieldParts().get(0) + this.mField.getFieldParts().get(1);
            System.out.println("M-Field (manufacturer): " + manufacturer);
        }
        if(this.aField != null) {
            String serialNr =  this.aField.getFieldParts().get(3) + this.aField.getFieldParts().get(2) +
                    this.aField.getFieldParts().get(1) + this.aField.getFieldParts().get(0);
            System.out.println("A-Field: ");
            System.out.println("\tSerialNumber: " + serialNr);
            System.out.println("\tVersion: " + this.aField.getFieldParts().get(4));
            System.out.println("\tType: " + this.aField.getFieldParts().get(5));
        }
        if(this.crcField != null) {
            System.out.println("CRC: " + this.crcField.getFieldParts().get(0) + " " +
                    "" + this.crcField.getFieldParts().get(1));
        }
    }

}