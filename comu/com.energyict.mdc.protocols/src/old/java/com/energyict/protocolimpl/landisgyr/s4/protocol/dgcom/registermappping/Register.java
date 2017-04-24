/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * Register.java
 *
 * Created on 12 juni 2006, 11:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.registermappping;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

/**
 *
 * @author Koen
 */
public class Register {

    private RegisterValue registerValue;
    private String description;

    /** Creates a new instance of Register */
    public Register(RegisterValue registerValue) {
        this(registerValue,"");
    }
    public Register(RegisterValue registerValue, String description) {
        this.setRegisterValue(registerValue);
        this.setDescription(registerValue.getObisCode().getDescription()+", "+description);
    }

    public String toString() {
        return getRegisterValue()+", "+getDescription();
    }

    public RegisterValue getRegisterValue() {
        return registerValue;
    }

    private void setRegisterValue(RegisterValue registerValue) {
        this.registerValue = registerValue;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }



}
