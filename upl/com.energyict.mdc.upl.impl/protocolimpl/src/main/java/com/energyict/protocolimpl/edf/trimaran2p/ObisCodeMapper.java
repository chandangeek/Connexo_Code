/**
 * 
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import java.io.IOException;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import com.energyict.protocolimpl.edf.trimarandlms.common.RegisterNameFactory;

/**
 * @author gna
 *
 */
public class ObisCodeMapper {
	
	Trimaran2P trimaran2P;

	/**
	 * 
	 */
	public ObisCodeMapper() {
	}

	public ObisCodeMapper(Trimaran2P trimaran2P) {
		this.trimaran2P = trimaran2P;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}
	
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	//TODO
    	//TODO 
    	//TODO 
    	//TODO 
    	//TODO 
    	//TODO 
        return new RegisterInfo(RegisterNameFactory.findObisCode(obisCode));
    }

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		
		Register register = getTrimaran2P().getRegisterFactory().findRegister(obisCode);
		
		return null;
	}

	/**
	 * @return the trimaran2P
	 */
	protected Trimaran2P getTrimaran2P() {
		return trimaran2P;
	}

}
