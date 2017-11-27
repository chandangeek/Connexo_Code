package com.energyict.mdc.masterdata.rest.impl;

import com.energyict.obis.ObisCode;

class ObisCodeInfo  {

    private final String obisValue;

    ObisCodeInfo(ObisCode obisCode) {
        this.obisValue = obisCode.getValue();
    }

    public String getObisValue(){
        return obisValue;
    }

}
