package com.energyict.protocolimpl.CM32;

import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.obis.ObisCode;

import java.io.IOException;


public class ObisCodeMapper {

	private CM32 cm32Protocol;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CM32 cm32Protocol) {
        this.cm32Protocol=cm32Protocol;
    }

    public String getRegisterInfo() throws IOException {
        return cm32Protocol.getRegisterFactory().getRegisterInfo();
    }


    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	return null;
    }


}

