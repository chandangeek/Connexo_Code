package com.energyict.protocolimpl.cm10;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegisterFactory {
	
	private CM10 cm10Protocol;
	private List registers;
	
	public RegisterFactory(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
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
	}

}

