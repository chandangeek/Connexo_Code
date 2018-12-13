/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.currencyconvertor.client;

import com.elster.jupiter.soap.currencyconverter.Currency;
import com.elster.jupiter.soap.currencyconverter.CurrencyConvertorSoap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Created by bvn on 5/9/16.
 */
@Component(name = "com.elster.jupiter.soap.currency.converter.client.gogo",
        service = CurrencyGogoCommands.class,
        property = {"osgi.command.scope=xe",
                "osgi.command.function=convert"},
        immediate = true)
public class CurrencyGogoCommands {

    private volatile CurrencyConvertorSoap soapService;

    public void convert() {
        System.out.println("Convert a currency");
        System.out.println("usage: convert <amount> <from> <to> ");
        System.out.println("   where <from> and <to> denote currencies, e.g. USD, EUR, GBP, <amount> denotes the amount to be converted, e.g. 16.25");
        System.out.println("example: xe.convert 16 EUR USD will convert 16 euro to $US");
    }

    public void convert(double amount, String from, String to) {
        double result = soapService.conversionRate(Currency.fromValue(from), Currency.fromValue(to));
        System.out.println(result * amount);
    }

    @Reference(target = "(url=http://www.webservicex.net/CurrencyConvertor.asmx?wsdl)")
//    @Reference(target = "(url=http://BE48LT4141B570:8080/CurrencyConvertor.asmx?wsdl)")
    public void setSoapService(CurrencyConvertorSoap soapService) {
        this.soapService = soapService;
    }
}
