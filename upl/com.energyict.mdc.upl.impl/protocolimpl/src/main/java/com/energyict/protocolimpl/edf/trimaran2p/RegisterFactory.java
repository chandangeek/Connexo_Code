/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import com.energyict.protocolimpl.edf.trimarandlms.common.VariableName;
import com.energyict.protocolimpl.edf.trimaranplus.core.VariableNameFactory;

/**
 * @author gna
 *
 */
public class RegisterFactory {
	
	Trimaran2P trimaran2P;
	private List registers;
	private ObisCode[] energiesB = {ObisCode.fromString("1.0.1.8.21.255"), ObisCode.fromString("1.0.2.8.21.255"),
									ObisCode.fromString("1.0.5.8.21.255"), ObisCode.fromString("1.0.6.8.21.255"),
									ObisCode.fromString("1.0.7.8.21.255"), ObisCode.fromString("1.0.8.8.21.255")};

	private ObisCode[] energiesN = {ObisCode.fromString("1.0.1.8.22.255"), ObisCode.fromString("1.0.2.8.22.255"),
									ObisCode.fromString("1.0.5.8.22.255"), ObisCode.fromString("1.0.6.8.22.255"),
									ObisCode.fromString("1.0.7.8.22.255"), ObisCode.fromString("1.0.8.8.22.255")};

	
	/**
	 * 
	 */
	public RegisterFactory() {
		// TODO Auto-generated constructor stub
	}

	public RegisterFactory(Trimaran2P trimaran2P) throws IOException {
		this.trimaran2P = trimaran2P;
		buildRegisters();
	}

	private void buildRegisters() throws IOException {
		registers = new ArrayList();
		buildEnergieRegisters();
		
	}

	private void buildEnergieRegisters() throws IOException {
		ObisCode obisCode = null;
		VariableName variableName = VariableNameFactory.getVariableName(56);
		for(int i = 0; i < 6; i++)
			registers.add(new Register(variableName, energiesB[i]));
		
		variableName = VariableNameFactory.getVariableName(64);
		for(int i = 0; i < 6; i++)
			registers.add(new Register(variableName, energiesN[i]));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public Register findRegister(ObisCode obc) throws NoSuchRegisterException {
        ObisCode obisCode = new ObisCode(obc.getA(),obc.getB(),obc.getC(),obc.getD(),obc.getE(),Math.abs(obc.getF()));
        Iterator it = getRegisters().iterator();
        while(it.hasNext()) {
            Register register = (Register)it.next();
            if (register.getObisCode().equals(obisCode)) {
                return register;
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}
	
    public List getRegisters() {
        return registers;
    }

}
