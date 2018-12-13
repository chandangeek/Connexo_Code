/*
 * ObisCodeMapper.java
 *
 * Created on 15 september 2006, 13:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    private final Kamstrup kamstrup;

    public ObisCodeMapper(Kamstrup kamstrup) {
        this.kamstrup=kamstrup;
    }

    public String getRegisterInfo() {
        StringBuilder builder = new StringBuilder();
        RegisterMappingFactory rmf = new RegisterMappingFactory();
        for (RegisterMapping rm : rmf.getRegisterMappings()) {
            builder.append(rm.getObisCode()).append(", ").append(rm.getDescription()).append(", ").append(rm.getRegisterCode()).append("\n");

        }
        return builder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        RegisterMappingFactory rmf = new RegisterMappingFactory();
        RegisterMapping rm = rmf.findRegisterMapping(obisCode);
        return new RegisterInfo(rm.getDescription()+", "+rm.getRegisterCode());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        RegisterMappingFactory rmf = new RegisterMappingFactory();
        String registerCode = rmf.findRegisterCode(obisCode);
        Object o = kamstrup.getKamstrupRegistry().getRegister(registerCode);
        if (o instanceof Quantity) {
            return new RegisterValue(obisCode,(Quantity)o);
        }
        else if (o instanceof Integer) {
            return new RegisterValue(obisCode,new Quantity((Integer)o,Unit.get("")));
        }
        else if (o instanceof String) {
            return new RegisterValue(obisCode,(String)o);
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

}