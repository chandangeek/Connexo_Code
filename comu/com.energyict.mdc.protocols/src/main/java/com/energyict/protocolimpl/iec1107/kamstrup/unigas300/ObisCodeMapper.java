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

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.iec1107.vdew.DateQuantityPair;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    private static final RegisterMappingFactory REGISTER_MAPPING_FACTORY = new RegisterMappingFactory();
    private Unigas300 unigas300;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public ObisCodeMapper(Unigas300 unigas300) {
        this.unigas300 = unigas300;
    }

    /**
     * @return
     */
    public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = REGISTER_MAPPING_FACTORY.getRegisterMappings().iterator();
        while (it.hasNext()) {
            RegisterMapping rm = (RegisterMapping) it.next();
            strBuff.append(rm.getObisCode() + ", " + rm.getDescription() + ", " + rm.getRegisterCode() + "\n");

        }
        return strBuff.toString();
    }

    /**
     * @param obisCode
     * @return
     * @throws IOException
     */
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        RegisterMapping rm = REGISTER_MAPPING_FACTORY.findRegisterMapping(obisCode);
        return new RegisterInfo(rm.getDescription() + ", " + rm.getRegisterCode());
    }

    /**
     * @param obisCode
     * @return
     * @throws IOException
     */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        String registerCode = REGISTER_MAPPING_FACTORY.findRegisterCode(obisCode);
        Object registerContent = getUnigas300Registry().getRegisterFromDevice(registerCode);

        if (registerContent instanceof Quantity) {
            return new RegisterValue(obisCode, (Quantity) registerContent);
        } else if (registerContent instanceof Integer) {
            return new RegisterValue(obisCode, new Quantity((Integer) registerContent, Unit.get("")));
        } else if (registerContent instanceof String) {
            return new RegisterValue(obisCode, (String) registerContent);
        } else if (registerContent instanceof DateQuantityPair) {
            return new RegisterValue(obisCode, ((DateQuantityPair) registerContent).getQuantity(), ((DateQuantityPair) registerContent).getDate());
        }

        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
    }

    /**
     * Getter for the unigas registry
     *
     * @return
     */
    private Unigas300Registry getUnigas300Registry() {
        return unigas300.getUnigas300Registry();
    }


}
