/*
 * Register.java
 *
 * Created on 15 september 2006, 9:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class Register {

    private int address;
    private int format;
    private ObisCode obisCode;
    private Unit unit;
    private String description;
    private int selfReadSet=-1;

    public static final int FORMAT_IEEE_32BIT_FP=0;
    public static final int FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP=1;
    public static final int FORMAT_INTEGER_16BIT=2;

    public Register(Register register, int address, int billingPoint, int selfReadSet) {
        this.setAddress(address);
        this.setFormat(register.getFormat());
        this.setObisCode(new ObisCode(register.getObisCode().getA(),register.getObisCode().getB(),register.getObisCode().getC(),register.getObisCode().getD(),register.getObisCode().getE(),billingPoint));
        this.setDescription(register.getDescription());
        this.setUnit(register.getUnit());
        this.setSelfReadSet(selfReadSet);
    }




    /** Creates a new instance of Register */
    public Register(int address, ObisCode obisCode) {
        this(address, FORMAT_IEEE_32BIT_FP, obisCode, obisCode.getDescription());
    }
    public Register(int address, ObisCode obisCode, String description) {
        this(address, FORMAT_IEEE_32BIT_FP, obisCode, description);
    }
    public Register(int address, int format, ObisCode obisCode) {
        this(address, format, obisCode, obisCode.getDescription());
    }
    public Register(int address, int format, ObisCode obisCode, String description) {
        this(address, format, obisCode, description, obisCode.getUnitElectricity(0));
    }

    public Register(int address, int format, ObisCode obisCode, String description, Unit unit) {
        this.setAddress(address);
        this.setFormat(format);
        this.setObisCode(obisCode);
        this.setDescription(description);
        this.setUnit(unit);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Register: 0x"+Integer.toHexString(getAddress())+", "+getDescription()+", "+getObisCode()+"\n");
        return strBuff.toString();
    }

    public int getLength() throws IOException {
        switch(getFormat()) {
            case FORMAT_IEEE_32BIT_FP: {
                return 4;
            }
            case FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP: {
                return 9;
            }
            case FORMAT_INTEGER_16BIT: {
                return 2;
            }
        }
        throw new IOException("Register, getLength(), error invalid format "+getFormat());
    }

    public BigDecimal getValue(byte[]data) throws IOException {
        switch(getFormat()) {
            case FORMAT_IEEE_32BIT_FP: {
                return new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,0,4)));
            }
            case FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP: {
                return new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,0,4)));
            }
            case FORMAT_INTEGER_16BIT: {
                return new BigDecimal(""+ProtocolUtils.getInt(data,0,2));
            }
        }
        throw new IOException("Register, getValue(...), error invalid format "+getFormat());
    }

    public Date getTimestamp(byte[]data, TimeZone timeZone) throws IOException {
        switch(getFormat()) {
            case FORMAT_IEEE_32BIT_FP: {
                return null;
            }
            case FORMAT_IEEE_32BIT_FP_WITH_5BYTE_TIMESTAMP: {
                return com.energyict.protocolimpl.itron.protocol.Utils.buildDate(data,4, timeZone);
            }
            case FORMAT_INTEGER_16BIT: {
                return null;
            }
        }
        throw new IOException("Register, getTimestamp(...), error invalid format "+getFormat());
    }


    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSelfReadSet() {
        return selfReadSet;
    }

    public void setSelfReadSet(int selfReadSet) {
        this.selfReadSet = selfReadSet;
    }

    public boolean isSelfReadRegister() {
        return selfReadSet != -1;
    }

}
