/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.device.data.RegisterInfo;
import com.energyict.mdc.protocol.device.data.RegisterValue;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    S4s s4s;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(S4s s4s) {
        this.s4s=s4s;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        if (read) {
            return s4s.getRegisterMapperFactory().getRegisterMapper().getRegisterValue(obisCode);
        }
        else {
            return obisCode.getDescription();
        }
    }
}
