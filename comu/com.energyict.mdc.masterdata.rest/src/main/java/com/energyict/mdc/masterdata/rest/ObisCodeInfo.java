package com.energyict.mdc.masterdata.rest;

import com.energyict.obis.ObisCode;

public class ObisCodeInfo {

    public String obisValue;

    public ObisCodeInfo() { }

    public ObisCodeInfo(ObisCode obisCode){
        this.obisValue = obisCode.getValue();
    }


}
