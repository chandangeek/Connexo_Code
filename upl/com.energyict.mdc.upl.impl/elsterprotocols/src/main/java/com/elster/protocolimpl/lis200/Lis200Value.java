package com.elster.protocolimpl.lis200;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;

/**
 * helper class for lis200 values that are in the form <value>"*"<unit>
 *  
 * @author heuckeg
 * @since 6/10/2010
 */
@SuppressWarnings({"unused"})
public class Lis200Value {

	/** field holding value */
	private String value;
	/** if rawData have had unit, then this unit is stored here */
	private Unit unit;

	/**
	 * Constructor for Lis200Value
	 * 
	 * @param rawData - String with data e.g. "100*m3"
	 */
	public Lis200Value(String rawData) {
		String[] data = rawData.split("[*]");
		value = data[0];
		if (data.length > 1) {
			unit = LIS200Utils.getUnitFromString(data[1].trim());
		}
		else {
			unit = Unit.getUndefined();
		}
	}

	/**
	 * Getter for value only
	 * @return value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Getter for unit. If there was no unit field, value of getUnit is Unit.Undefined
	 * @return unit
	 */
	public Unit getUnit() {
		return unit;
	}

	/**
	 * Getter for value as Quantity
	 * 
	 * @return quantity
	 */
	public Quantity toQuantity() {
		return new Quantity(value, unit);
	}
}
