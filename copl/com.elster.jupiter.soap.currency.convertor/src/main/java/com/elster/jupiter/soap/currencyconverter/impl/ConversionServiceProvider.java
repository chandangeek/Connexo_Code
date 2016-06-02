package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import java.net.URL;
import java.util.List;

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
    public Service get(URL wsdlUrl, List<WebServiceFeature> webServiceFeatures) {
        try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
            return new CurrencyConvertor(wsdlUrl, webServiceFeatures.toArray(new WebServiceFeature[webServiceFeatures.size()]));
        }
    }

    @Override
    public Class getService() {
        return CurrencyConvertorSoap.class;
    }
}
