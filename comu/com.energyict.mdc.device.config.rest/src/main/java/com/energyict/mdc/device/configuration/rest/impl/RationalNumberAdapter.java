package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.util.Checks;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class RationalNumberAdapter extends XmlAdapter<String, RationalNumber> {

    public static final String SEPARATOR = "/";

    @Override
    public RationalNumber unmarshal(String stringValue) throws Exception {
        if (Checks.is(stringValue).emptyOrOnlyWhiteSpace()) {
            return RationalNumber.NOTAPPLICABLE;
        }
        try {
            int index = stringValue.indexOf(SEPARATOR);
            long numerator = Long.valueOf(stringValue.substring(0, index));
            long denominator = Long.valueOf(stringValue.substring(index+1));
            return new RationalNumber(numerator, denominator);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert '"+stringValue+"' into a valid RationalNumber", e);
        }
    }

    @Override
    public String marshal(RationalNumber rationalNumber) throws Exception {
        return rationalNumber.getNumerator()+"/"+rationalNumber.getDenominator();
    }
}
