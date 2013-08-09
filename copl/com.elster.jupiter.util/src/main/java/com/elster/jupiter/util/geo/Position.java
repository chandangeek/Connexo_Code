package com.elster.jupiter.util.geo;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlJavaTypeAdapter(PositionAdapter.class)
final public class Position {
	private static final double MEANEARTHRADIUS = 6371009.0;
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
		return centralAngle(other) * MEANEARTHRADIUS;
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
	
	
}
