package com.elster.insight.usagepoint.data.rest;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.elster.jupiter.util.Checks;

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
