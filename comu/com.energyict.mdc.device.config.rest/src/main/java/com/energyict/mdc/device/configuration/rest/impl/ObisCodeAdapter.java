package com.energyict.mdc.device.configuration.rest.impl;

import com.energyict.mdc.common.ObisCode;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ObisCodeAdapter extends XmlAdapter<String, ObisCode> {

    @Override
    public ObisCode unmarshal(String jsonValue) throws Exception {
        return ObisCode.fromString(jsonValue);
    }

    @Override
    public String marshal(ObisCode obisCode) throws Exception {
        return obisCode.toString();
    }
}
