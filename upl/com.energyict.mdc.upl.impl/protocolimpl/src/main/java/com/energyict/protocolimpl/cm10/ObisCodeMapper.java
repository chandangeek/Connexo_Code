package com.energyict.protocolimpl.cm10;

import java.io.IOException;

import com.energyict.obis.ObisCode;

import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;


public class ObisCodeMapper {
	
	private CM10 cm10Protocol;
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CM10 cm10Protocol) {
        this.cm10Protocol=cm10Protocol;
    }
    
    public String getRegisterInfo() throws IOException {
        return cm10Protocol.getRegisterFactory().getRegisterInfo();
    }
    
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	return null;
    }
       

}

