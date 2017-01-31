/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Currency;

public class CurrencyAdapter extends XmlAdapter<String, Currency>{

    @Override
    public Currency unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return Currency.getInstance(jsonValue);
    }

    @Override
    public String marshal(Currency currency) throws Exception {
        if (currency==null) {
            return null;
        }
        return currency.toString();
    }

}