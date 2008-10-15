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

import com.energyict.cbo.Unit;

/**
 * @author jme
 *
 */
public class ChannelTypeParser {

	int DecimalPointScaling;
	int ScalingFactor;
	int Type;
	Unit unit;
	
	public ChannelTypeParser(int ChannelDef) {
    	this.DecimalPointScaling = (ChannelDef & 0x7000) >> 12;
    	this.ScalingFactor = (ChannelDef & 0x0C00) >> 10;
    	this.Type = 'F';
        RegisterUnitParser rup = new RegisterUnitParser();
    	this.unit = rup.parse('R');
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

	public Unit getUnit() {
		return unit;
	}	
}
