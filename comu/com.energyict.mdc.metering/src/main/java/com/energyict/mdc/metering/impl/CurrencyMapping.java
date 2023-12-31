/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import java.util.Optional;

import java.util.Currency;
import java.util.Optional;

enum CurrencyMapping {

    POUND(BaseUnit.POUND, Currency.getInstance("GBP")),
    EURO(BaseUnit.EURO, Currency.getInstance("EUR")),
    USDOLLAR(BaseUnit.USD, Currency.getInstance("USD")),
    EGP(BaseUnit.EGP, Currency.getInstance("EGP"));

    private final int baseUnit;
    private final Currency currency;

    CurrencyMapping(int baseUnit, Currency currency) {
        this.baseUnit = baseUnit;
        this.currency = currency;
    }

    public static Optional<Currency> getCurrencyFor(Unit unit){
        if(Optional.ofNullable(unit).isPresent()){
            for (CurrencyMapping currencyMapping : values()) {
                if(currencyMapping.baseUnit == unit.getDlmsCode()){
                    return Optional.of(currencyMapping.currency);
                }
            }
        }
        return Optional.empty();
    }

    int getBaseUnit() {
        return baseUnit;
    }

    Currency getCurrency() {
        return currency;
    }
}
