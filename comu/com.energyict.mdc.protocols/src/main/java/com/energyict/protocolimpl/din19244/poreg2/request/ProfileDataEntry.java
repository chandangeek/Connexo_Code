package com.energyict.protocolimpl.din19244.poreg2.request;

import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 5-mei-2011
 * Time: 13:57:02
 */
public class ProfileDataEntry {

    private Date date;
    private int status;
    private ExtendedValue value;
    private int gid;
    private int registerAddress;
    private int fieldAddress;
    private int length;

    public ProfileDataEntry(int length, int gid, int registerAddress, int fieldAddress, Date date, int status, ExtendedValue value) {
        this.length = length;
        this.registerAddress = registerAddress;
        this.fieldAddress = fieldAddress;
        this.gid = gid;
        this.date = date;
        this.status = status;
        this.value = value;
    }

    public int getLength() {
        return length;
    }

    public Date getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    public ExtendedValue getValue() {
        return value;
    }

    public int getFieldAddress() {
        return fieldAddress;
    }

    public int getGid() {
        return gid;
    }

    public int getRegisterAddress() {
        return registerAddress;
    }
}
