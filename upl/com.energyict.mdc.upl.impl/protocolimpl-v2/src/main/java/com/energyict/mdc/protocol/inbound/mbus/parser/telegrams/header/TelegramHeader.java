/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.header;


import com.energyict.mdc.protocol.inbound.mbus.MerlinLogger;
import com.energyict.mdc.protocol.inbound.mbus.parser.telegrams.TelegramField;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.Arrays;
import java.util.StringJoiner;

public class  TelegramHeader {

    private final MerlinLogger logger;
    private TelegramField lField;
    private TelegramField cField;
    private TelegramField mField;
    private TelegramField aField;
    private TelegramField crcField;
    public static final int HEADER_LENGTH_CRC = 11;
    public static final int HEADER_LENGTH_NO_CRC = 9;

    public TelegramHeader(MerlinLogger logger) {
        this.logger = logger;
    }

    public void createTelegramHeader(String header) {
        this.createTelegramHeader(header.split(" "));
    }

    public void createTelegramHeader(String[] header) {
        this.setLField(header[0]);
        this.setCField(header[1]);
        this.setMField(Arrays.copyOfRange(header, 2, 5));
        this.setAField(Arrays.copyOfRange(header, 5, 10));
        if (header.length == TelegramHeader.HEADER_LENGTH_CRC) {
            this.setCRCField(Arrays.copyOfRange(header, 10, 11));
        }
    }

    /**
     * Information from Sameer
     *                  AN  AN  AN  AN  Numeric ...
     *                  --  --  --  --  --  --  --  --  --  --  --  --  --  --
     * 14 bytes buffer  1   2   3   4   5   6   7   8   9   10  11  12  13  14
     * wWBus DLL            1   2   3   4       5       6       7       8
     *                      ~~~~~~~~~   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *                        ASCII     HEX String
     */
    public String getSerialNr() {
        try {
            String asciiPart = ProtocolTools.getAsciiFromBytes(mField.getFieldAsByteArray());
            String hexPart = ProtocolTools.getHexStringFromBytes(this.aField.getFieldAsByteArray(),"");
            return asciiPart + hexPart;
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
        this.lField = new TelegramField(logger);
        this.lField.addFieldPart(lField);
    }

    public void setCField(String cField) {
        this.cField = new TelegramField(logger);
        this.cField.addFieldPart(cField);
    }

    public void setMField(String[] mField) {
        this.mField = new TelegramField(logger);
        this.mField.addFieldParts(mField);
    }

    public void setAField(String[] aField) {
        this.aField = new TelegramField(logger);
        this.aField.addFieldParts(aField);
    }

    public void setCRCField(String[] crcField) {
        this.crcField = new TelegramField(logger);
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

    public void debugOutput(StringJoiner joiner) {
        if (this.lField != null) {
            joiner.add("Length of Telegram: " + this.lField.getFieldParts().get(0));
        }
        if (this.cField != null) {
            joiner.add("C-Field (mode): " + this.cField.getFieldParts().get(0));
        }
        if (this.mField != null) {
            String manufacturer =  this.mField.getFieldParts().get(0) + this.mField.getFieldParts().get(1);
            joiner.add("M-Field (manufacturer): " + manufacturer);
        }
        if (this.aField != null) {
            String serialNr =  this.aField.getFieldParts().get(3) + this.aField.getFieldParts().get(2) +
                    this.aField.getFieldParts().get(1) + this.aField.getFieldParts().get(0);
            joiner.add("A-Field: ");
            joiner.add("\tSerialNumber: " + serialNr);
            joiner.add("\tVersion: " + this.aField.getFieldParts().get(4));
            //joiner.add("\tType: " + this.aField.getFieldParts().get(5));
        }
        if (this.crcField != null) {
            joiner.add("CRC: " + this.crcField.getFieldParts().get(0) + " " +
                    "" + this.crcField.getFieldParts().get(1));
        }
    }

}