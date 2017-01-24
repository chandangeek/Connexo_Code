/*
 * ChannelTypeParser.java
 *
 * Created on 15 oktober 2008, 16:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.core;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.math.BigDecimal;

/**
 * @author jme
 * Changes:
 * GNA |26022009| Added the units for instantaneous values
 */
public class SurveyChannelTypeParser {

	private int DecimalPointScaling;
	private int ScalingFactor;
	private int Type;
	private int instantType;
	private String Name;
	private Unit unit;
	private boolean channelenabled = true;
	private boolean instant = false;

	private final static int VOLTS = 0;
	private final static int AMPS = 1;
	private final static int POWER = 2;
	private final static int ANGLE = 3;
	private final static int FREQ = 4;

	public SurveyChannelTypeParser(int ChannelDef) {
		this.DecimalPointScaling = (ChannelDef & 0x7000) >> 12;
		int regvalue = ChannelDef & 0x000F;
		int regfunction = (ChannelDef & 0x00F0) >> 4;
		int scaling = (ChannelDef & 0x0C00) >> 10;

		boolean isEnergy = false;
		boolean isPulse = false;
		boolean isInstantaneous = false;
		boolean isReserved = false;
		boolean isTesting = false;


		if ((ChannelDef & 0x00FF) == 0x00FF) {
			channelenabled = false;
		}

		RegisterUnitParser rup = new RegisterUnitParser();

		switch(regfunction) {
		case 0x00:
			isEnergy = true;
			this.Name = "All";
			break;
		case 0x01:
			isEnergy = true;
			this.Name = "Import"; // import and export conform with IEC and not ANSI
			break;
		case 0x02:
			isEnergy = true;
			this.Name = "Export"; // import and export conform with IEC and not ANSI
			break;
		case 0x03:
			isEnergy = true;
			this.Name = "Q1";
			break;
		case 0x04:
			isEnergy = true;
			this.Name = "Q2";
			break;
		case 0x05:
			isEnergy = true;
			this.Name = "Q3";
			break;
		case 0x06:
			isEnergy = true;
			this.Name = "Q4";
			break;
		case 0x07:
			isPulse = true;
			this.Name = "Pulsing input " + String.valueOf(regvalue);
			break;
		case 0x08: case 0x09:
			isInstantaneous = true;
			this.Name = "5 cycle readings";
			break;
		case 0x0A: case 0x0B:
			isInstantaneous = true;
			this.Name = "5 cycle readings, frequency compensated";
			break;
		case 0x0C: case 0x0D:
			isPulse = true;
			this.Name = "Extra pulse input channels";
			break;
		case 0x0E:
			isReserved = true;
			this.Name = "Reserved";
			break;
		case 0x0F:
			isTesting = true;
			this.Name = "Reserved for testfunctions";
			break;
		default:
			this.Name = "";
		}

		if (isEnergy) {
			this.ScalingFactor = 1;
			switch(regvalue) {
			case 0x00:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = this.Name + " A phase " + this.unit.toString();
				break;
			case 0x01:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = this.Name + " B phase " + this.unit.toString();
				break;
			case 0x02:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = this.Name + " C phase " + this.unit.toString();
				break;
			case 0x03:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = this.Name + " Total  " + this.unit.toString();
				break;
			case 0x04:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = this.Name + " A phase " + this.unit.toString();
				break;
			case 0x05:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = this.Name + " B phase " + this.unit.toString();
				break;
			case 0x06:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = this.Name + " C phase " + this.unit.toString();
				break;
			case 0x07:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, scaling * 3); // Channel value base unit is varh
				this.Name = this.Name + " Total " + this.unit.toString();
				break;
			case 0x08:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = this.Name + " A phase " + this.unit.toString();
				break;
			case 0x09:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = this.Name + " B phase " + this.unit.toString();
				break;
			case 0x0A:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = this.Name + " C phase " + this.unit.toString();
				break;
			case 0x0B:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREHOUR, scaling * 3); // Channel value base unit is Vah
				this.Name = this.Name + " Total " + this.unit.toString();
				break;
			default:
				this.unit = Unit.get(BaseUnit.UNITLESS, scaling * 3); // Channel base unit unknown (reserved). No base unit
			} // End of switch(regvalue)
		} // End of if(isEnergy)

		if(isTesting) {
			this.ScalingFactor = 1;
			switch(regvalue) {
			case 0x00:
				this.unit = Unit.get(BaseUnit.WATTHOUR, scaling * 3); // Channel value base unit is Wh
				this.Name = this.Name + " Test Power source in " + this.unit.toString();
				break;
			case 0x01:
				this.unit = Unit.get(BaseUnit.KELVIN, scaling * 3); // Channel value base unit is Kelvin
				this.Name = this.Name + " Temperature in " + this.unit.toString();
				break;
			default:
				this.unit = rup.parse('N'); // Channel base unit unknown (reserved). No base unit
			} // End of switch(regvalue)
		} // End of if(isTesting)


		if(isInstantaneous){
			this.ScalingFactor = 1;
			this.instant = true;
			regvalue = regvalue + (regfunction << 4)&0x1F;

			switch(regvalue) {
			case 0x00:
			case 0x01:
			case 0x02:
				this.unit = Unit.get(BaseUnit.AMPERE); // Channel value base unit is current ABC
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.AMPS;
				break;
			case 0x03:
			case 0x04:
			case 0x05:
				this.unit = Unit.get(BaseUnit.VOLT); // Channel value base unit is voltage ABC
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.VOLTS;
				break;
			case 0x06:
			case 0x07:
			case 0x08:
				this.unit = Unit.get(BaseUnit.WATT); // Channel value base unit is watt ABC
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;
				break;
			case 0x09:
			case 0x0A:
			case 0x0B:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); // Channel value base unit is var ABC
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;
				break;
			case 0x0C:
			case 0x0D:
			case 0x0E:
				this.unit = Unit.get(BaseUnit.VOLTAMPERE); // Channel value base unit is VA ABC
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;
				break;
			case 0x0F:
				this.unit = Unit.get(BaseUnit.HERTZ); // Channel value base unit is frequency
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.FREQ;
				break;
			case 0x10:
				this.unit = Unit.get(BaseUnit.AMPERE); // Channel value base unit is current ABC average
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.AMPS;
				break;
			case 0x11:
				this.unit = Unit.get(BaseUnit.VOLT); // Channel value base unit is voltage ABC average
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.VOLTS;
				break;
			case 0x12:
				this.unit = Unit.get(BaseUnit.WATT); // Channel value base unit is watt sum
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;
				break;
			case 0x13:
				this.unit = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); // Channel value base unit is var sum
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;
				break;
			case 0x14:
				this.unit = Unit.get(BaseUnit.VOLTAMPERE); // Channel value base unit is VA sum
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;
				break;
			case 0x15:
			case 0x16:
			case 0x17:
				this.unit = Unit.get(BaseUnit.DEGREE); // Channel value base unit is angles ABC
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.ANGLE;
				break;
			case 0x18:
				this.unit = Unit.get(BaseUnit.DEGREE); // Channel value base unit is Voltage angle A-B
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.ANGLE;
				break;
			case 0x19:
				this.unit = Unit.get(BaseUnit.DEGREE); // Channel value base unit is Voltage angle A-C
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.ANGLE;
				break;
			default:
				this.unit = Unit.get(BaseUnit.UNITLESS); // Channel value base unit is default
				this.Name = this.Name + " Min/Max/Inst/Avg " + this.unit.toString();
				this.instantType = this.POWER;	// it's a choice ...
				break;
			}
		}

		if(isPulse || isReserved) {
			this.ScalingFactor = 10^scaling;
			this.unit = Unit.get(BaseUnit.UNITLESS); // Channel base unit unknown (reserved). No base unit
		}

		this.Type = 'F';

	}

	public int getDecimalPointScaling() {
		return DecimalPointScaling;
	}

	public BigDecimal getScalingFactor() {
		return new BigDecimal(ScalingFactor);
	}

	public int getType() {
		return Type;
	}

	public boolean isChannel(){
		return channelenabled;
	}

	public Unit getUnit() {
		return unit;
	}

	public String getName() {
		return Name;
	}

	public boolean isInstantaneous(){
		return this.instant;
	}

	public int getInstantaneousType(){
		return this.instantType;
	}
}
