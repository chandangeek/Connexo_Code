/*
 * RegisterMapping.java
 *
 * Created on 15 september 2006, 13:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

import com.energyict.obis.ObisCode;

/**
 *
 * @author Koen
 */
public class RegisterMapping {

    private String registerCode;
    private ObisCode obisCode;
    private String description;

    /** Creates a new instance of RegisterMapping */
    public RegisterMapping(String description,String registerCode, ObisCode obisCode) {
        this.setDescription(description);
        this.setRegisterCode(registerCode);
        this.setObisCode(obisCode);
    }

    public String getRegisterCode() {
        return registerCode;
    }

    public void setRegisterCode(String registerCode) {
        this.registerCode = registerCode;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
