/*
 * ObisCodeMapper.java
 *
 * Created on 20 juli 2005, 10:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic.core;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;

import java.io.IOException;
import java.util.Iterator;
/**
 *
 * @author koen
 */
public class ObisCodeMapper {

    Alpha alpha;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(Alpha alpha) {
        this.alpha=alpha;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        int set=0;

        if (obisCode.getF() == 255) set = BillingDataRegisterFactoryImpl.CURRENT_BILLING_REGISTERS;
        else if (obisCode.getF() == 0) set = BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS;
        else if (obisCode.getF() == 1) set = BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

        Iterator it = alpha.getBillingDataRegisterFactory().getBillingDataRegisters(set).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            if (bdr.getObisCode().equals(obisCode)) {
                return bdr.getRegisterValue();
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
}
