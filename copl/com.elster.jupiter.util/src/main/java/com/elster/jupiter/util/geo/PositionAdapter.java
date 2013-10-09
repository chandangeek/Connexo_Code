package com.elster.jupiter.util.geo;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public final class PositionAdapter extends XmlAdapter<AdaptedPosition,Position> {

	@Override
	public Position unmarshal(AdaptedPosition v) {
		return new Position(v.latitude,v.longitude);		
	}

	@Override
	public AdaptedPosition marshal(Position v) {
		return v == null ? null : new AdaptedPosition(v);
	}

}
