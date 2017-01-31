/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class RationalNumberAdapter extends XmlAdapter<String, RationalNumber> {

    public static final String SEPARATOR = "/";
    static final String NOT_APPLICABLE = "Not applicable";

    @Override
    public RationalNumber unmarshal(String stringValue) throws Exception {
        if (Checks.is(stringValue).emptyOrOnlyWhiteSpace() || NOT_APPLICABLE.equals(stringValue)) {
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
        if (rationalNumber.equals(RationalNumber.NOTAPPLICABLE)) {
            return NOT_APPLICABLE;
        }
        return rationalNumber.getNumerator()+"/"+rationalNumber.getDenominator();
    }
}
