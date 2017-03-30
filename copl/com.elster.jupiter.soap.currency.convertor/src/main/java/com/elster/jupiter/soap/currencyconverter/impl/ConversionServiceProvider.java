/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundSoapEndPointProvider;

import org.osgi.service.component.annotations.Component;

import javax.xml.ws.Service;

/**
 * Created by bvn on 5/9/16.
 */
@Component(name = "com.elster.jupiter.soap.currency.converter.provider",
        service = {OutboundSoapEndPointProvider.class},
        immediate = true,
        property = {"name=Currency exchange"})
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
