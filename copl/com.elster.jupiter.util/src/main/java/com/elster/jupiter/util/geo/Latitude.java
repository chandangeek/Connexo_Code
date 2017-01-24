package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public final class Latitude extends Angle {

	public Latitude(BigDecimal value) {
		super(value);
	}
	
	@Override
	public String toString() {
		return baseString() + (signum() < 0 ? "S" : "N");
	}
}
