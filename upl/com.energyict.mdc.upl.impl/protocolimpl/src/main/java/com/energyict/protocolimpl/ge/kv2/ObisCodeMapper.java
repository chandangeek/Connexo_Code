/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.ansi.c12.AbstractResponse;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;

import java.io.IOException;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    private final GEKV2 gekv2;

    public ObisCodeMapper(GEKV2 gekv2) {
        this.gekv2 = gekv2;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        if (read) {
            try {
                return gekv2.getObisCodeInfoFactory().getRegister(obisCode);
            } catch (ResponseIOException e) {
                // table does not exist!
                if (e.getReason() == AbstractResponse.IAR) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported! (" + e.toString() + ")");
                } else {
                    throw e;
                }
            }
        } else {
            return gekv2.getObisCodeInfoFactory().getRegisterInfo(obisCode);
        }
    }

}