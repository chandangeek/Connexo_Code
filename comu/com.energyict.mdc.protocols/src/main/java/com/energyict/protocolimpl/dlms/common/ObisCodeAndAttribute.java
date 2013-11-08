package com.energyict.protocolimpl.dlms.common;

import com.energyict.obis.ObisCode;

import java.io.Serializable;

/**
 * Copyrights EnergyICT
 * Date: 24/08/12
 * Time: 15:04
 * Author: khe
 */
public class ObisCodeAndAttribute implements Serializable {

    private int attribute;
    private ObisCode obisCode;

    public ObisCodeAndAttribute(int attribute, ObisCode obisCode) {
        this.attribute = attribute;
        this.obisCode = obisCode;
    }
}
