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

import java.math.BigDecimal;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 * @author jme
 *
 */
public class SurveyChannelTypeParser {

	private int DecimalPointScaling;
	private int ScalingFactor;
	private int Type;
	private String Name;
	private Unit unit; 
	private boolean channelenabled = true;
	
	public SurveyChannelTypeParser(int ChannelDef) {
		this.DecimalPointScaling = (ChannelDef & 0x7000) >> 12;
    	int regvalue = ChannelDef & 0x000F;
    	int regfunction = (ChannelDef & 0x00F0) >> 4;
    	int scaling = (ChannelDef & 0x0C00) >> 10;
    	
    	boolean isEnergy = false;
    	boolean isPulse = false;
    	boolean isCycle = false;
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
        	isCycle = true;
            this.Name = "5 cycle readings";
            break;
        case 0x0A: case 0x0B:
        	isCycle = true;
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
        
    	if(isPulse || isCycle || isReserved) {
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
}
