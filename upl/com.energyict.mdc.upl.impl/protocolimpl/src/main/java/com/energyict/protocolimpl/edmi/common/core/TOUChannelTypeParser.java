package com.energyict.protocolimpl.edmi.common.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;
/**
 * @author jme
 *
 */
public class TOUChannelTypeParser {

	private int decimalPointScaling;
	private int ScalingFactor;
	private int Type;
	private int rates;
	private int obisCField = 0;

	private String Name;
	private Unit unit;
	private boolean channelEnabled = true;

	private int scaling;

	public TOUChannelTypeParser(int ChannelDef) {
		this.decimalPointScaling = (ChannelDef & 0xE000) >> 13;
		this.rates = ((ChannelDef & 0x0700) >> 8) + 1;
		this.scaling = (ChannelDef & 0x1800) >> 11;
		int regValue = ChannelDef & 0x000F;
		int regFunction = (ChannelDef & 0x00F0) >> 4;

		boolean isEnergy = false;
		boolean isPulse = false;
		boolean isCycle = false;
		boolean isReserved = false;
		boolean isTesting = false;

		if ((ChannelDef & 0x00FF) == 0x00FF) {
			channelEnabled = false;
		}

		switch(regFunction) {
		case 0x00:
			isEnergy = true;
			this.Name = "abs ";
			break;
		case 0x01:
			isEnergy = true;
			this.Name = "import "; // import and export conform with IEC and not ANSI
			break;
		case 0x02:
			isEnergy = true;
			this.Name = "export "; // import and export conform with IEC and not ANSI
			break;
		case 0x03:
			isEnergy = true;
			this.Name = "Q1 ";
			break;
		case 0x04:
			isEnergy = true;
			this.Name = "Q2 ";
			break;
		case 0x05:
			isEnergy = true;
			this.Name = "Q3 ";
			break;
		case 0x06:
			isEnergy = true;
			this.Name = "Q4 ";
			break;
		case 0x07:
			isPulse = true;
			this.Name = "Pulsing input " + String.valueOf(regValue);
			break;
		case 0x08: case 0x09:
			isCycle = true;
			this.Name = "5 cycle readings ";
			break;
		case 0x0A: case 0x0B:
			isCycle = true;
			this.Name = "5 cycle readings, frequency compensated ";
			break;
		case 0x0C: case 0x0D:
			isPulse = true;
			this.Name = "Extra pulse input channels ";
			break;
		case 0x0E:
			isReserved = true;
			this.Name = "Reserved ";
			break;
		case 0x0F:
			isTesting = true;
			this.Name = "Reserved for testfunctions ";
			break;
		default:
			this.Name = " ";
		}

		if (isEnergy) {
			this.ScalingFactor = 1;
			switch(regValue) {
			case 0x00:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = "L1 active " + this.Name + this.unit.toString();
				break;
			case 0x01:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = "L2 active " + this.Name + this.unit.toString();
				break;
			case 0x02:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = "L3 active " + this.Name + this.unit.toString();
				break;
			case 0x03:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = "Total active " + this.Name + this.unit.toString();
				break;
			case 0x04:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = "L1 reactive " + this.Name + this.unit.toString();
				break;
			case 0x05:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = "L2 reactive " + this.Name + this.unit.toString();
				break;
			case 0x06:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = "L3 reactive " + this.Name + this.unit.toString();
				break;
			case 0x07:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = "Total reactive " + this.Name + this.unit.toString();
				break;
			case 0x08:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = "L1 apparent " + this.Name + this.unit.toString();
				break;
			case 0x09:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = "L2 apparent " + this.Name + this.unit.toString();
				break;
			case 0x0A:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = "L3 apparent " + this.Name + this.unit.toString();
				break;
			case 0x0B:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = "Total apparent " + this.Name + this.unit.toString();
				break;
			default:
				this.unit = Unit.get(BaseUnit.UNITLESS, scaling * 3); // Channel base unit unknown (reserved). No base unit
			} // End of switch(regvalue)


			this.obisCField = parseNameToObisC(this.Name);

		} // End of if(isEnergy)

		if(isTesting) {
			this.ScalingFactor = 1;
			switch(regValue) {
			case 0x00:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = this.Name + " Test Power source in " + this.unit.toString();
				break;
			case 0x01:
				this.unit = Unit.get(BaseUnit.KELVIN, scaling * 3); // Channel value base unit is Kelvin
				this.Name = this.Name + " Temperature in " + this.unit.toString();
				break;
			default:
				this.unit = RegisterUnitParser.parse('N'); // Channel base unit unknown (reserved). No base unit
			} // End of switch(regvalue)
		} // End of if(isTesting)

		if(isPulse || isCycle || isReserved) {
			this.ScalingFactor = 10^scaling;
			this.unit = Unit.get(BaseUnit.UNITLESS); // Channel base unit unknown (reserved). No base unit
		}

		this.Type = 'F';

	}

	private int parseNameToObisC(String name) {
		int obisc = 0;
		name = name.toLowerCase();

		if (name.contains("active")) {
			if (name.contains("import")) {
				obisc = 1;
			}
			if (name.contains("export")) {
				obisc = 2;
			}
			if (name.contains("abs")) {
				obisc = 128;
			}
			if (name.contains("q1")) {
				obisc = 17;
			}
			if (name.contains("q2")) {
				obisc = 18;
			}
			if (name.contains("q3")) {
				obisc = 19;
			}
			if (name.contains("q4")) {
				obisc = 20;
			}
		}
		if (name.contains("reactive")) {
			if (name.contains("import")) {
				obisc = 3;
			}
			if (name.contains("export")) {
				obisc = 4;
			}
			if (name.contains("abs")) {
				obisc = 129;
			}
			if (name.contains("q1")) {
				obisc = 5;
			}
			if (name.contains("q2")) {
				obisc = 6;
			}
			if (name.contains("q3")) {
				obisc = 7;
			}
			if (name.contains("q4")) {
				obisc = 8;
			}
		}
		if (name.contains("apparent")) {
			if (name.contains("import")) {
				obisc = 9;
			}
			if (name.contains("export")) {
				obisc = 10;
			}
			if (name.contains("abs")) {
				obisc = 130;
			}
			if (name.contains("q1")) {
				obisc = 138;
			}
			if (name.contains("q2")) {
				obisc = 134;
			}
			if (name.contains("q3")) {
				obisc = 142;
			}
			if (name.contains("q4")) {
				obisc = 146;
			}
		}

		// Obis code not found !!!!!
		if (obisc == 0) {return -1;}

		if (name.contains("apparent q")) {
			if (name.contains("l1")) {
				obisc += 0;
			}
			if (name.contains("l2")) {
				obisc += 1;
			}
			if (name.contains("l3")) {
				obisc += 2;
			}
			if (name.contains("total")) {
				obisc += 3;
			}
		}
		else {
			if (name.contains("l1")) {
				obisc += 20;
			}
			if (name.contains("l2")) {
				obisc += 40;
			}
			if (name.contains("l3")) {
				obisc += 60;
			}
			if (name.contains("total")) {
				obisc += 0;
			}
		}
		return obisc;
	}

	public int getRates() {
		return rates;
	}

	public int getDecimalPointScaling() {
		return decimalPointScaling;
	}

	public int getScaling() {
		return scaling;
	}

	public BigDecimal getScalingFactor() {
		return new BigDecimal(ScalingFactor);
	}

	public int getType() {
		return Type;
	}

	public Unit getUnit() {
		return unit;
	}

	public boolean isChannel(){
		return channelEnabled;
	}

	public String getName() {
		return Name;
	}

	public int getObisCField() {
		return obisCField;
	}
}