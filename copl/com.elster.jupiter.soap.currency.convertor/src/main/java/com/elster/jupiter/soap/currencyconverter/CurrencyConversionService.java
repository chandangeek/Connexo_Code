package com.elster.jupiter.soap.currencyconverter;

/**
 * Created by bvn on 5/9/16.
 */
public interface CurrencyConversionService {
    /**
     * Converts a currency to another
     *
     * @param fromCurrency The currency to convert from, 3 letter code, e.g. USD, EUR, GBP
     * @param toCurrency The currency to convert to, 3 leter code, e.g. EUR, USD, GBP
     * @param amount The amount in the original currency to be converted
     * @return The equivalent amount in the toCurrency
     */
    public double convert(String fromCurrency, String toCurrency, double amount);
}
