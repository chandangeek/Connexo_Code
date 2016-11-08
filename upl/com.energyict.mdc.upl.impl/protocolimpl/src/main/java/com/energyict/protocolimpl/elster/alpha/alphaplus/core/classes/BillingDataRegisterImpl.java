/*
 * BillingDataRegister.java
 *
 * Created on 20 juli 2005, 9:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.elster.alpha.BillingDataRegister;


/**
 *
 * @author koen
 */
public class BillingDataRegisterImpl implements BillingDataRegister {

    private final String description;
    private final ObisCode obisCode;
    private final RegisterValue registerValue;

    BillingDataRegisterImpl(ObisCode obisCode, String description, RegisterValue registerValue) {
        this.obisCode=obisCode;
        this.description=description;
        this.registerValue=registerValue;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ObisCode getObisCode() {
        return obisCode;
    }

    @Override
    public RegisterValue getRegisterValue() {
        return registerValue;
    }

}