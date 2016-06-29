package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;

import org.osgi.service.component.annotations.Component;

import javax.xml.ws.Service;

/**
 * Created by bvn on 5/9/16.
 */
@Component(name = "com.elster.jupiter.soap.currency.converter.provider",
        service = {OutboundEndPointProvider.class},
        immediate = true,
        property = {"name=xe"})
public class ConversionServiceProvider implements OutboundEndPointProvider {

    @Override
    public Service get() {
        return new CurrencyConvertor();
    }

    @Override
    public Class getService() {
        return CurrencyConvertorSoap.class;
    }
}
