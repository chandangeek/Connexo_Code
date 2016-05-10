package com.elster.jupiter.soap.currencyconvertor.client;

import com.elster.jupiter.soap.currencyconverter.CurrencyConversionService;

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

    private volatile CurrencyConversionService currencyConvertService;

    public void convert() {
        System.out.println("Convert a currency");
        System.out.println("usage: convert <amount> <from> <to> ");
        System.out.println("   where <from> and <to> denote currencies, e.g. USD, EUR, GBP, <amount> denotes the amount to be converted, e.g. 16.25");
        System.out.println("example: xe.convert 16 EUR USD will convert 16 euro to $US");
    }

    public void convert(double amount, String from, String to) {
        double result = currencyConvertService.convert(from, to, amount);
        System.out.println(result);
    }

    @Reference
    public void setCurrencyConvertService(CurrencyConversionService currencyConvertService) {
        this.currencyConvertService = currencyConvertService;
    }
}
