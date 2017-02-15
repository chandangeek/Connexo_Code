/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 19 juni 2006, 16:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.edf.trimarancje.registermapping.Register;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.RegisterNameFactory;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    Trimaran trimeran;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Trimaran trimeran) {
        this.trimeran=trimeran;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(RegisterNameFactory.findObisCode(obisCode));
    }


    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        Register register = trimeran.getRegisterFactory().findRegister(obisCode);
        if (register != null){
            return register.getRegisterValue();
        }

        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    } // private Object getRegisterValue(ObisCode obisCode, boolean read)




}
