/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.geo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Objects;

@XmlJavaTypeAdapter(PositionAdapter.class)
@XmlRootElement
public final class Position {
	private static final double MEAN_EARTH_RADIUS = 6371009.0;

	private final Latitude latitude;
	private final Longitude longitude;
	
	public Position(BigDecimal latitudeValue, BigDecimal longitudeValue) {
		this.latitude = new Latitude(latitudeValue);
		this.longitude = new Longitude(longitudeValue);
	}
	
	/**
	 * 
	 * Calculates central angle in radians between receiver and argument
	 * Use haversine formula which is numerically better conditioned for small distances
	 * than the simpler arccos(sin(lat1)*sin(lat2) + cos(lat1)*cos(lat2)*cos(long1 - long2))
	 * 
	 */
	private double centralAngle(Position other) {
		double latitudeDelta = this.latitude.subtract(other.latitude).toRadians();
		double longitudeDelta = this.longitude.subtract(other.longitude).toRadians();
		return 2.0 * Math.asin(Math.sqrt(
				Math.pow(Math.sin(latitudeDelta/2),2) +
				this.latitude.cos() * other.latitude.cos() * Math.pow(Math.sin(longitudeDelta/2), 2)));
	}
	
	/**
	 * returns great circle distance between receiver and argument in meters
	 */
	public double distance(Position other) {
		return centralAngle(other) * MEAN_EARTH_RADIUS;
	}
	
	public Latitude getLatitude() {
		return latitude;
	}
	
	public Longitude getLongitude() {
		return longitude;
	}
	
	public String toString() {
		return latitude.toString() + " " + longitude.toString();
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Position position = (Position) o;

        return latitude.equals(position.latitude) && longitude.equals(position.longitude);

    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}
