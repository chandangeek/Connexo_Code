package com.elster.jupiter.util.units;

import java.util.Currency;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class MoneyAdapter extends XmlAdapter<AdaptedMoney,Money> {

	@Override
	public Money unmarshal(AdaptedMoney v) throws Exception {
		Currency currency = Currency.getInstance(v.currency);
		return new Money(currency,v.value);
	}

	@Override
	public AdaptedMoney marshal(Money v) throws Exception {		
		return v == null ? null : new AdaptedMoney(v);
	}

}
