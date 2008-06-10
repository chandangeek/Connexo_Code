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
import com.energyict.protocolimpl.edf.trimaran2p.core.VariableNameFactory;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import com.energyict.protocolimpl.edf.trimarandlms.common.VariableName;

/**
 * @author gna
 *
 */
public class RegisterFactory {
	
	Trimaran2P trimaran2P;
	private List registers;
//	private ObisCode[] energiesB = {ObisCode.fromString("1.0.1.8.21.255"), ObisCode.fromString("1.0.2.8.21.255"),
//									ObisCode.fromString("1.0.5.8.21.255"), ObisCode.fromString("1.0.6.8.21.255"),
//									ObisCode.fromString("1.0.7.8.21.255"), ObisCode.fromString("1.0.8.8.21.255")};
//
//	private ObisCode[] energiesN = {ObisCode.fromString("1.0.1.8.22.255"), ObisCode.fromString("1.0.2.8.22.255"),
//									ObisCode.fromString("1.0.5.8.22.255"), ObisCode.fromString("1.0.6.8.22.255"),
//									ObisCode.fromString("1.0.7.8.22.255"), ObisCode.fromString("1.0.8.8.22.255")};
	
	private ObisCode tempFonctionne = ObisCode.fromString("0.1.96.8.1.255");
	
	private int[] index = {1, 2, 5, 6 ,7, 8};
	private int[] indexBrut = {140, 141, 142, 143, 144, 145};
	private int offsetJour = 10;
	private int offsetMois = 20;

	
	/**
	 * 
	 */
	public RegisterFactory() {
	}

	public RegisterFactory(Trimaran2P trimaran2P) throws IOException {
		this.trimaran2P = trimaran2P;
		buildRegisters();
	}

	private void buildRegisters() throws IOException {
		registers = new ArrayList();
		buildEnergieRegisters();
		buildTempFonctionnementRegisters();
		buildArretesRegisters();
	}

	private void buildTempFonctionnementRegisters() throws IOException {
		VariableName variableName = VariableNameFactory.getVariableName(152);
		registers.add(new Register(variableName, tempFonctionne));
	}

	private void buildEnergieRegisters() throws IOException {
		VariableName variableName = VariableNameFactory.getVariableName(56);
		for(int i = 0; i < 6; i++){
			variableName.setObisFField(255);	
			variableName.setObisCField(indexBrut[i]);					//manufacturer specific (brut)
			registers.add(new Register(variableName, i, 0));
		}
		
		variableName = VariableNameFactory.getVariableName(64); 	// Nette standard!
		for(int i = 0; i < 6; i++){
			variableName.setObisFField(255);
			variableName.setObisCField(index[i]);					
			registers.add(new Register(variableName, i, 0));
		}
	}
	
	private void buildArretesRegisters() throws IOException{
		VariableName variableName;
		for(int i = 0; i < 6; i++){
			
			for(int j = 0; j < 4; j++){
				variableName = VariableNameFactory.getVariableName(104);
				variableName.setObisCField(index[i]);		
				variableName.setObisFField(j);						//Les indexProgJour
				registers.add(new Register(variableName, i, 0));
				if(j < 2){
					variableName = VariableNameFactory.getVariableName(120);
					variableName.setObisCField(index[i]);		
					variableName.setObisFField(j+offsetJour);
					registers.add(new Register(variableName, i, 0));
					variableName = VariableNameFactory.getVariableName(128);
					variableName.setObisCField(index[i]);		
					variableName.setObisFField(j+offsetMois);
					registers.add(new Register(variableName, i, 0));
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
