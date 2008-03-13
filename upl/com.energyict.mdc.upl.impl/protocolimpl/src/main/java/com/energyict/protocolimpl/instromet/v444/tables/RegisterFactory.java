package com.energyict.protocolimpl.instromet.v444.tables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.protocolimpl.instromet.v444.Instromet444;
import com.energyict.protocolimpl.instromet.v555.Instromet555;
import com.energyict.obis.*;

public class RegisterFactory {
	
	private Instromet444 instromet444;
	private List registers;
	
	public RegisterFactory(Instromet444 instromet444) {
		this.instromet444 = instromet444;
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
		unCorrectedVolume.setDescription("Uncorrected gas volume (m�)");
		Register correctedVolume = new Register(new ObisCode(  7,0,11,1 ,0,55));
		correctedVolume.setDescription("Uncorrected gas volume (m�)");
		Register peak = new Register(new ObisCode(  7,0,11,5 ,0,55));
		peak.setDescription("peak day (m�/h)");
		registers.add(unCorrectedVolume);
		registers.add(correctedVolume);
		registers.add(peak);
	}

}
