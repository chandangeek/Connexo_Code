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

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;


/**
 *
 * @author koen
 */
public class BillingDataRegister {

    String description;
    ObisCode obisCode;
    RegisterValue registerValue;

    /** Creates a new instance of BillingDataRegister */
    public BillingDataRegister(ObisCode obisCode,String description,RegisterValue registerValue) {
        this.obisCode=obisCode;
        this.description=description;
        this.registerValue=registerValue;
    }

    public String getDescription() {
        return description;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public RegisterValue getRegisterValue() {
        return registerValue;
    }

}
