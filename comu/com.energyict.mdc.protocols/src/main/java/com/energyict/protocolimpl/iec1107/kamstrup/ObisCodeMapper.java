/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    Kamstrup kamstrup;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Kamstrup kamstrup) {
        this.kamstrup=kamstrup;
    }


    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        RegisterMappingFactory rmf = new RegisterMappingFactory();
        Iterator it = rmf.getRegisterMappings().iterator();
        while(it.hasNext()) {
            RegisterMapping rm = (RegisterMapping)it.next();
            strBuff.append(rm.getObisCode()+", "+rm.getDescription()+", "+rm.getRegisterCode()+"\n");

        }
        return strBuff.toString();
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
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
