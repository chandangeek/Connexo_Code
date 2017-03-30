/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;


import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigDecimal;

public class BigDecimalAsStringAdapter extends XmlAdapter<String, BigDecimal>{

    @Override
    public BigDecimal unmarshal(String v) throws Exception {
        if (Checks.is(v).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return new BigDecimal(v);
    }

    @Override
    public String marshal(BigDecimal v) throws Exception {
        return v != null ? v.toString() : "";
    }
}
