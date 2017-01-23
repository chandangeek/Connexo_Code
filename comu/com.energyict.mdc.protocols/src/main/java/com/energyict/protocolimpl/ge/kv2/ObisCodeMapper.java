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

    GEKV2 gekv2;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(GEKV2 gekv2) {
        this.gekv2=gekv2;
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
               return gekv2.getObisCodeInfoFactory().getRegister(obisCode);
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.IAR) // table does not exist!
                   throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! ("+e.toString()+")");
                else
                   throw e;
            }
        }
        else {
            return gekv2.getObisCodeInfoFactory().getRegisterInfo(obisCode);
        }
    }
}
