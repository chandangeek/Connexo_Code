/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import java.math.BigDecimal;

public final class AdaptedMoney {
	
	public BigDecimal value;
	public String currency;
	
	public AdaptedMoney() {		
	}

	AdaptedMoney(Money money) {
		this.value = money.getValue();
		this.currency = money.getCurrency().getCurrencyCode();
	}
}
