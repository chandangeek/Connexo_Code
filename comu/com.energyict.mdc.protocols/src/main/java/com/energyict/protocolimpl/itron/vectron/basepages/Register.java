/*
 * Register.java
 *
 * Created on 15 september 2006, 9:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.obis.ObisCode;

/**
 *
 * @author Koen
 */
public class Register {

    private int address;
    private int address2;
    private ObisCode obisCode;
    private RegisterConfig registerConfig;
    private int length;
    private boolean floatingBCD;


    public Register(int address, ObisCode obisCode, RegisterConfig registerConfig) {
        this(address,-1,obisCode, registerConfig);
    }

    public Register(int address, int address2, ObisCode obisCode, RegisterConfig registerConfig) {
        this.setAddress(address);
        this.setAddress2(address2);
        this.setObisCode(obisCode);
        this.setRegisterConfig(registerConfig);

        if (getObisCode().getF()!=255) {
            setLength(3);
            setFloatingBCD(true);
        }
        else if ((getObisCode().getF()==255) && (getObisCode().getD()==ObisCode.CODE_D_TIME_INTEGRAL1)) {
            setLength(7);
            setFloatingBCD(false);
        }
        else {
            setLength(4);
            setFloatingBCD(true);
        }


    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("Register: "+getObisCode()+", address 0x"+Integer.toHexString(getAddress())+", "+getRegisterConfig());
        return strBuff.toString();
    }

    public int getLength() {
        return length;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public RegisterConfig getRegisterConfig() {
        return registerConfig;
    }

    public void setRegisterConfig(RegisterConfig registerConfig) {
        this.registerConfig = registerConfig;
    }

    public int getAddress2() {
        return address2;
    }

    public void setAddress2(int address2) {
        this.address2 = address2;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isFloatingBCD() {
        return floatingBCD;
    }

    public boolean isSelfRead() {
        return (getObisCode().getF() >= 0) && (getObisCode().getF() <= 3);
    }

    public void setFloatingBCD(boolean floatingBCD) {
        this.floatingBCD = floatingBCD;
    }


}
