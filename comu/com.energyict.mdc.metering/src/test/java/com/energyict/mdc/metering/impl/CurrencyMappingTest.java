/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.metering.impl;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import java.util.Currency;
import java.util.Optional;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class CurrencyMappingTest {

    @Test
    public void usdTest() {
        Unit usDollar = Unit.get(BaseUnit.USD);
        Optional<Currency> usd = CurrencyMapping.getCurrencyFor(usDollar);

        assertThat(usd.isPresent()).isTrue();
        assertThat(usd.get().getCurrencyCode()).isEqualTo("USD");
        assertThat(usd.get().getNumericCode()).isEqualTo(840);
    }

    @Test
    public void bgpTest() {
        Unit pounds = Unit.get(BaseUnit.POUND);
        Optional<Currency> gbp = CurrencyMapping.getCurrencyFor(pounds);

        assertThat(gbp.isPresent()).isTrue();
        assertThat(gbp.get().getCurrencyCode()).isEqualTo("GBP");
        assertThat(gbp.get().getNumericCode()).isEqualTo(826);
    }

    @Test
    public void eurTest() {
        Unit pounds = Unit.get(BaseUnit.EURO);
        Optional<Currency> eur = CurrencyMapping.getCurrencyFor(pounds);

        assertThat(eur.isPresent()).isTrue();
        assertThat(eur.get().getCurrencyCode()).isEqualTo("EUR");
        assertThat(eur.get().getNumericCode()).isEqualTo(978);
    }

    @Test
    public void unknownCurrencyTest() {
        Unit liters = Unit.get(BaseUnit.LITER);
        Optional<Currency> litre = CurrencyMapping.getCurrencyFor(liters);

        assertThat(litre.isPresent()).isFalse();
    }

    @Test
    public void nullSafeCheckTest() {
        Optional<Currency> nullSafe = CurrencyMapping.getCurrencyFor(null);

        assertThat(nullSafe.isPresent()).isFalse();
    }
}
