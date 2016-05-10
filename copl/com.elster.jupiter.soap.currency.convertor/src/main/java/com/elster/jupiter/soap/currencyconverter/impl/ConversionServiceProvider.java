package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.whiteboard.InboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;

import org.osgi.service.component.annotations.Component;

/**
 * Created by bvn on 5/9/16.
 */
@Component(name = "com.elster.jupiter.soap.currency.converter.provider", service = {InboundEndPointProvider.class}, immediate = true, property = {"name=xe"})
public class ConversionServiceProvider implements OutboundEndPointProvider {
    @Override
    public Object get() {
        return new CurrencyConversionServiceImpl();
    }

    @Override
    public String[] getServices() {
        return new String[]{"com.elster.jupiter.soap.currencyconverter.CurrencyConversionService"};
    }
}
