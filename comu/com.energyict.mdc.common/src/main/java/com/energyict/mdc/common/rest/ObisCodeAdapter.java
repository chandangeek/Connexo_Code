/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.energyict.obis.ObisCode;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ObisCodeAdapter extends XmlAdapter<String, ObisCode> {

    @Override
    public ObisCode unmarshal(String jsonValue) throws Exception {
        return ObisCode.fromString(jsonValue);
    }

    @Override
    public String marshal(ObisCode obisCode) throws Exception {
        if (obisCode==null) {
            return null;
        }
        return obisCode.toString();
    }
}
