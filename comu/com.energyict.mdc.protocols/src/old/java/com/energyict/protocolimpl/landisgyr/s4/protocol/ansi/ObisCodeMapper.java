/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.ansi.c12.AbstractResponse;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    S4 s4;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(S4 s4) {
        this.s4=s4;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        if (read) {
            try {
               return s4.getObisCodeInfoFactory().getRegister(obisCode);
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported! (" + e.toString() + ")");
                }
                else {
                    throw e;
                }
            }
        }
        else {
            return s4.getObisCodeInfoFactory().getRegisterInfo(obisCode);
        }
    }
}
