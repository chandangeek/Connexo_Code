/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.common;

import com.energyict.mdc.common.ObisCode;

import java.io.Serializable;

public class ObisCodeAndAttribute implements Serializable {

    private int attribute;
    private ObisCode obisCode;

    public ObisCodeAndAttribute(int attribute, ObisCode obisCode) {
        this.attribute = attribute;
        this.obisCode = obisCode;
    }
}
