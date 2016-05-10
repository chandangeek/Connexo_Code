package com.elster.jupiter.soap.currencyconverter.impl;

import com.elster.jupiter.soap.currencyconverter.CurrencyConversionService;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 5/9/16.
 */
//@Component(name = "com.elster.jupiter.currency.converter", service = CurrencyConversionService.class, immediate = true)
public class CurrencyConversionServiceImpl implements CurrencyConversionService {
    private SoapProviderSupportFactory soapProviderSupportFactory;

    public CurrencyConversionServiceImpl(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Reference
    public void setSoapProviderSupportFactory(SoapProviderSupportFactory soapProviderSupportFactory) {
        this.soapProviderSupportFactory = soapProviderSupportFactory;
    }

    @Override
    public double convert(String fromCurrency, String toCurrency, double amount) {
        try (ContextClassLoaderResource ctx = soapProviderSupportFactory.create()) {
            CurrencyConvertor currencyConvertor = new CurrencyConvertor();
            CurrencyConvertorSoap currencyConvertorSoap = currencyConvertor.getCurrencyConvertorSoap();
            Currency fromCurrency1 = Currency.valueOf(fromCurrency);
            Currency toCurrency1 = Currency.valueOf(toCurrency);
            double rate = currencyConvertorSoap.conversionRate(fromCurrency1, toCurrency1);
            return amount * rate;
        }
    }
}
