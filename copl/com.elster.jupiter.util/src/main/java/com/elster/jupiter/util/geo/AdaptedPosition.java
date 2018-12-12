/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import java.math.BigDecimal;

public final class AdaptedPosition {
	public BigDecimal latitude;
	public BigDecimal longitude;

	public AdaptedPosition() {		
	}

	AdaptedPosition(Position position) {
		this.latitude = position.getLatitude().getValue();
		this.longitude = position.getLongitude().getValue();
	}
}
