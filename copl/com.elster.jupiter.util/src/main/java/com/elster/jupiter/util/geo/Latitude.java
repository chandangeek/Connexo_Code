package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

final public class Latitude extends Angle {
	
	Latitude(BigDecimal value) {
		super(value);
	}
	
	@Override
	public String toString() {
		return baseString() + (signum() < 0 ? "S" : "N");
	}
}
