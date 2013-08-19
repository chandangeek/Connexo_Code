package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public final class Longitude extends Angle {
	
	Longitude(BigDecimal value) {
		super(value);
	}
	
	@Override
	public String toString() {
		return baseString() + (signum() < 0 ? "W" : "E");
	}
}
