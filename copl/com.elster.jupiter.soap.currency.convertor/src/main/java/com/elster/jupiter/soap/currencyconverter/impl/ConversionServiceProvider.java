/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import javax.xml.ws.Service;

/**
 * Created by bvn on 5/9/16.
 */

// Based on WebserviceX.NET currency converter that does not work anymore.
// Please don't uncomment this as this service breaks the dropdown list with web services in Connexo
// Or if it is needed for some reason, pls try moving towards fixer.io or some other service

//@Component(name = "com.elster.jupiter.soap.currency.converter.provider",
//        service = {OutboundSoapEndPointProvider.class},
//        immediate = true,
//        property = {"name=Currency exchange"})
public class ConversionServiceProvider implements OutboundSoapEndPointProvider {

    @Override
    public Service get() {
        return new CurrencyConvertor();
    }

    @Override
    public Class getService() {
        return CurrencyConvertorSoap.class;
    }
}
