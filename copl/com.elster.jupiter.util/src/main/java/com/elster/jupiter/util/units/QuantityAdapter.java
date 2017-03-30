/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.units;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class QuantityAdapter extends XmlAdapter<AdaptedQuantity,Quantity> {

	@Override
	public Quantity unmarshal(AdaptedQuantity v) {
		if (v.value == null || v.unit == null) {
			return null;
		}
		Unit unit = Unit.get(v.unit);
		return unit.amount(v.value,v.multiplier);
	}

	@Override
	public AdaptedQuantity marshal(Quantity v) {
		return v == null ? null : new AdaptedQuantity(v);
	}

}
