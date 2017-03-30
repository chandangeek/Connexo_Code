/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Currency;

public final class MoneyAdapter extends XmlAdapter<AdaptedMoney,Money> {

	@Override
	public Money unmarshal(AdaptedMoney v) {
		Currency currency = Currency.getInstance(v.currency);
		return new Money(currency,v.value);
	}

	@Override
	public AdaptedMoney marshal(Money v) {
		return v == null ? null : new AdaptedMoney(v);
	}

}
