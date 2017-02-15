/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeDescriptor.java
 *
 * Created on 1 december 2005, 9:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2.tables;

import com.energyict.mdc.common.ObisCode;

/**
 *
 * @author koen
 */
public class ObisCodeDescriptor {

    private String description;
    private ObisCode obisCode;

    /** Creates a new instance of ObisCodeDescriptor */
    public ObisCodeDescriptor(ObisCode obisCode,String description) {
        this.setObisCode(obisCode);
        this.setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    private void setDescription(String description) {
        this.description = description;
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    private void setObisCode(ObisCode obisCode) {
        this.obisCode = obisCode;
    }

}
