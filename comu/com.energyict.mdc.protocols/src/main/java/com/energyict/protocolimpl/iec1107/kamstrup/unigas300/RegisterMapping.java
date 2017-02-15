/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterMapping.java
 *
 * Created on 15 september 2006, 13:38
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.mdc.common.ObisCode;

public class RegisterMapping {

    private final String registerCode;
    private final ObisCode obisCode;
    private final String description;

    /**
     * Creates a new instance of RegisterMapping
     */
    public RegisterMapping(String description, String registerCode, ObisCode obisCode) {
        this.description = description;
        this.registerCode = registerCode;
        this.obisCode = obisCode;
    }

    /**
     * Getter for the register code. This is the VDEW register address used by the device
     * (ex. C.8.0 or C.91.4, ...)
     *
     * @return
     */
    public String getRegisterCode() {
        return registerCode;
    }

    /**
     * Getter for the obiscode used in eiserver
     *
     * @return
     */
    public ObisCode getObisCode() {
        return obisCode;
    }

    /**
     * Getter for the register description, user readable
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

}
