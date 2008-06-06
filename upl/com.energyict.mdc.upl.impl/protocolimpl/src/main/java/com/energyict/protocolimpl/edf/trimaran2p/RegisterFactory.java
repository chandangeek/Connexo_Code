/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import java.util.Iterator;
import java.util.List;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.edf.trimaran2p.Trimaran2P;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;

/**
 * @author gna
 *
 */
public class RegisterFactory {
	
	Trimaran2P trimaran2P;
	private List registers;

	/**
	 * 
	 */
	public RegisterFactory() {
		// TODO Auto-generated constructor stub
	}

	public RegisterFactory(Trimaran2P trimaran2P) {
		this.trimaran2P = trimaran2P;
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
