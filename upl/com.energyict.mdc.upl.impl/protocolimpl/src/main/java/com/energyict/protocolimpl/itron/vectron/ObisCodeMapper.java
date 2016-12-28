/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.itron.vectron.basepages.Register;
import com.energyict.protocolimpl.itron.vectron.basepages.RegisterBasePage;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    private final Vectron vectron;

    public ObisCodeMapper(Vectron vectron) {
        this.vectron=vectron;
    }

    public String getRegisterInfo() throws IOException {
        return  vectron.getRegisterFactory().getRegisterInfo();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Register register = vectron.getRegisterFactory().findRegisterByObisCode(obisCode);
        RegisterBasePage rbp = vectron.getBasePagesFactory().getRegisterBasePage(register);

        //System.out.println("KV_DEBUG> "+rbp);

        return new RegisterValue(register.getObisCode(),rbp.getQuantity(),rbp.getDate(),rbp.getSelfReadDate());
    }

}