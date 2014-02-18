package com.energyict.mdc.metering.impl;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.google.common.base.Optional;

import java.util.Currency;

/**
 * The <i>Currency</i> is defined by CIM as follow:
 * <p>
 * Currency codes are defined in ISO 4217
 * </p>
 * <p/>
 *
 * {@link BaseUnit} contains several <i>currencies</i>.
 * These are mapped in this enumeration.
 *
 * Copyrights EnergyICT
 * Date: 26/11/13
 * Time: 15:29
 */
enum CurrencyMapping {

    POUND(BaseUnit.POUND, Currency.getInstance("GBP")),
    EURO(BaseUnit.EURO, Currency.getInstance("EUR")),
    USDOLLAR(BaseUnit.USD, Currency.getInstance("USD"));

    private final int baseUnit;
    private final Currency currency;

    CurrencyMapping(int baseUnit, Currency currency) {
        this.baseUnit = baseUnit;
        this.currency = currency;
    }

    public static Optional<Currency> getCurrencyFor(Unit unit){
        if(Optional.fromNullable(unit).isPresent()){
            for (CurrencyMapping currencyMapping : values()) {
                if(currencyMapping.baseUnit == unit.getDlmsCode()){
                    return Optional.of(currencyMapping.currency);
                }
            }
        }
        return Optional.absent();
    }

    int getBaseUnit() {
        return baseUnit;
    }

    Currency getCurrency() {
        return currency;
    }
}
