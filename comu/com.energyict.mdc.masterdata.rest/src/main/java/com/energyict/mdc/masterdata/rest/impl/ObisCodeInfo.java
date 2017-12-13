package com.energyict.mdc.masterdata.rest.impl;

class ObisCodeInfo  {

    private final String obisCode;

    ObisCodeInfo(String obisCode) {
        this.obisCode = obisCode;
    }

    public String getObisValue(){
        return obisCode;
    }

}
