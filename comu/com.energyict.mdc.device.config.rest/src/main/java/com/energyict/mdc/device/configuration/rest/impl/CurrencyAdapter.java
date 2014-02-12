package com.energyict.mdc.device.configuration.rest.impl;

import java.util.Currency;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CurrencyAdapter extends XmlAdapter<String, Currency>{

    @Override
    public Currency unmarshal(String jsonValue) throws Exception {
        return Currency.getInstance(jsonValue);
    }

    @Override
    public String marshal(Currency currency) throws Exception {
        return currency.toString();
    }
}
