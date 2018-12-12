/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Currency;

@XmlRootElement
@XmlJavaTypeAdapter(MoneyAdapter.class)

/**
 * Immutable class representing an amount of money in one currency.
 */
public final class Money {
	private final Currency currency;
	private final BigDecimal value;
	
	@SuppressWarnings("unused")
	private Money() {		
		currency = null;
		value = null;
	}
	
	Money(Currency currency, BigDecimal value ) {
		this.currency = currency;
		this.value = value;	
	}

	public Currency getCurrency() {
		return currency;
	}

	public BigDecimal getValue() {
		return value;
	}

	
	@Override
	public String toString() {		
		return "" + value + " " + currency.getSymbol();
	}
	
}
