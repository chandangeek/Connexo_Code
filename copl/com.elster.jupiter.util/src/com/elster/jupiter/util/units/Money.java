package com.elster.jupiter.util.units;

import java.math.BigDecimal;
import java.util.Currency;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(MoneyAdapter.class)

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
	
	public static void main(String[] args) {
		for (Currency each : Currency.getAvailableCurrencies()) {
			Money money = new Money(each, BigDecimal.valueOf(12.45));
			System.out.println(money);
		}
	}
}
