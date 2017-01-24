/*
 * ObisCodeMapper.java
 *
 * Created on 16 november 2005, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    S200 s200;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(S200 s200) {
        this.s200=s200;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        if ((obisCode.getA()==1)&&(obisCode.getC()==82)&&(obisCode.getD()==8)&&(obisCode.getE()==0)&&(obisCode.getF()==255)) {
            if (read) {
                if (obisCode.getB() <= s200.getNumberOfChannels()) {
                    return new RegisterValue(obisCode,new Quantity(s200.getCommandFactory().getMeterDataCommand(obisCode.getB()-1).getBigDecimalCumulativePulseCount(), Unit.get("")));
                }
            } else {
                return new RegisterInfo(obisCode.getDescription());
            }
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
}
