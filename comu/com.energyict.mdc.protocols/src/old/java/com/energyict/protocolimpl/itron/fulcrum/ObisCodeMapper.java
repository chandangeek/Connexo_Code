/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.itron.fulcrum.basepages.Register;
import com.energyict.protocolimpl.itron.fulcrum.basepages.RegisterBasePage;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    Fulcrum fulcrum;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Fulcrum fulcrum) {
        this.fulcrum=fulcrum;
    }


    public String getRegisterInfo() throws IOException {
        return fulcrum.getRegisterFactory().getRegisterInfo();
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {

        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));

        Register register = fulcrum.getRegisterFactory().findRegisterByObisCode(obisCode);
        RegisterBasePage rbp = fulcrum.getBasePagesFactory().getRegisterBasePage(register);
        if (register.isSelfReadRegister())
           return new RegisterValue(register.getObisCode(),rbp.getQuantity(),rbp.getTimestamp(),rbp.getFromTimestamp());
        else
           return new RegisterValue(register.getObisCode(),rbp.getQuantity(),rbp.getTimestamp());
    }


}
