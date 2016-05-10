package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.whiteboard.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 5/9/16.
 */
@Component(name = "com.elster.jupiter.soap.currency.converter.provider",
        service = {OutboundEndPointProvider.class},
        immediate = true,
        property = {"name=xe"})
public class ConversionServiceProvider implements OutboundEndPointProvider {

    private volatile SoapProviderSupportFactory soapProviderSupportFactory;

    @Reference
    public void setSoapProviderSupportFactory(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public Object get() {
        return new CurrencyConversionServiceImpl(soapProviderSupportFactory);
    }

    @Override
    public String[] getServices() {
        return new String[]{"com.elster.jupiter.soap.currencyconverter.CurrencyConversionService"};
    }
}
