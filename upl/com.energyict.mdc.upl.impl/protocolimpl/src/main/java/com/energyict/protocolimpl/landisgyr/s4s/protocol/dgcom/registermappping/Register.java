/*
 * Register.java
 *
 * Created on 12 juni 2006, 11:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.registermappping;

import com.energyict.protocol.RegisterValue;

/**
 *
 * @author Koen
 */
public class Register {

    private RegisterValue registerValue;
    private String description;

    public Register(RegisterValue registerValue) {
        this(registerValue, "");
    }
    public Register(RegisterValue registerValue, String description) {
        this.setRegisterValue(registerValue);
        this.setDescription(registerValue.getObisCode().toString() + ", " + description);
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