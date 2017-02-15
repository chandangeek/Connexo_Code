/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.v555.tables;

import com.energyict.mdc.common.ObisCode;

import com.energyict.protocolimpl.instromet.v555.Instromet555;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegisterFactory {

	private Instromet555 instromet555;
	private List registers;

	public RegisterFactory(Instromet555 instromet555) {
		this.instromet555 = instromet555;
	}

	public void init() {
		buildRegisters();
	}

	public String getRegisterInfo() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            strBuff.append(""+r);
        }
        return strBuff.toString();
    }

	protected void buildRegisters() {
		registers = new ArrayList();
		Register unCorrectedVolume = new Register(new ObisCode(7,0,11,10,0,55));
		unCorrectedVolume.setDescription("Uncorrected gas volume (m³)");
		Register correctedVolume = new Register(new ObisCode(  7,0,11,1 ,0,55));
		correctedVolume.setDescription("Uncorrected gas volume (m³)");
		Register peak = new Register(new ObisCode(  7,0,11,5 ,0,55));
		peak.setDescription("peak day (m³/h)");
		registers.add(unCorrectedVolume);
		registers.add(correctedVolume);
		registers.add(peak);
	}

}
