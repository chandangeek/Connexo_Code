package com.energyict.mdc.device.configuration.rest.impl;

import java.util.Currency;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CurrencyAdapter extends XmlAdapter<Currency, String>{

    @Override
    public String unmarshal(Currency currency) throws Exception {
        return currency.toString();
    }

    @Override
    public Currency marshal(String jsonValue) throws Exception {
        return Currency.getInstance(jsonValue);
    }
}
