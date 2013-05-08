package com.elster.jupiter.util.units;

import java.math.BigDecimal;

public final class AdaptedQuantity {
	
	public BigDecimal value;
	public String unit;
	public int multiplier;

	public AdaptedQuantity() {		
	}

	AdaptedQuantity(Quantity quantity) {
		this.value = quantity.getValue();
		this.unit = quantity.getUnit().getSymbol();
		this.multiplier = quantity.getMultiplier();
	}
}
