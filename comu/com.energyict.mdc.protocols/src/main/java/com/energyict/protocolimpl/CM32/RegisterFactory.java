package com.energyict.protocolimpl.CM32;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RegisterFactory {
	
	private CM32 cm32Protocol;
	private List registers;
	
	public RegisterFactory(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
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

