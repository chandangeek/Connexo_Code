package com.elster.protocolimpl.dsfg;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

public class DsfgUtils {

	/**
	 * Convert the given String to the respective {@link Unit}.<br>
	 * Implemented units:<br>
	 * <li> {@link BaseUnit#CUBICMETER} <li> {@link BaseUnit#WATTHOUR} <li>
	 * {@link BaseUnit#WATT} <br>
	 * <br>
	 * The last two can have a scaler of 3 when 'k' is added in the string
	 * 
	 * @param strUnit
	 *            - the given strUnit
	 * 
	 * @return the Unit
	 */
	public static Unit getUnitFromString(String strUnit) {
		int scaler = 0;
		if (strUnit.equalsIgnoreCase("m3")) {
			return Unit.get(BaseUnit.CUBICMETER);
		}
		else if (strUnit.equalsIgnoreCase("bar")) {
			return Unit.get(BaseUnit.BAR);
		}
		else if ((strUnit.equalsIgnoreCase("{C"))
				|| (strUnit.equalsIgnoreCase("C"))
				|| (strUnit.equalsIgnoreCase("celsius"))
				|| (strUnit.equalsIgnoreCase("Grad C"))) {
			return Unit.get(BaseUnit.DEGREE_CELSIUS);
		}
		else if ((strUnit.equalsIgnoreCase("{F"))
				|| (strUnit.equalsIgnoreCase("F"))) {
			return Unit.get(BaseUnit.FAHRENHEIT);
		}
		else if (strUnit.indexOf("Wh") > -1) {
			scaler = (strUnit.indexOf("k") > -1) ? 3 : 0;
			return Unit.get(BaseUnit.WATTHOUR, scaler);
		}
		else if (strUnit.indexOf("W") > -1) {
			scaler = (strUnit.indexOf("k") > -1) ? 3 : 0;
			return Unit.get(BaseUnit.WATT, scaler);
		}
		else if (strUnit.indexOf("m3|h") > -1) {
			return Unit.get(BaseUnit.CUBICMETERPERHOUR);
		}
		else {
			return Unit.getUndefined();
		}
	}

}
