/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.itron.quantum.basepages.Register;
import com.energyict.protocolimpl.itron.quantum.basepages.RegisterBasePage;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    private final Quantum quantum;

    public ObisCodeMapper(Quantum quantum) {
        this.quantum=quantum;
    }

    public String getRegisterInfo() throws IOException {
        return  quantum.getRegisterFactory().getRegisterInfo();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obc) throws IOException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Register register = quantum.getRegisterFactory().findRegisterByObisCode(obisCode);
        RegisterBasePage rbp = quantum.getBasePagesFactory().getRegisterBasePage(register);
        return new RegisterValue(register.getObisCode(),rbp.getQuantity(),null,rbp.getSelfReadDate());
    }


}
