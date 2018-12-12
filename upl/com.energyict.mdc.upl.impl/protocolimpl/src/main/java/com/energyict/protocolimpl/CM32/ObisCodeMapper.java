package com.energyict.protocolimpl.CM32;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

public class ObisCodeMapper {

    private final CM32 cm32Protocol;

    public ObisCodeMapper(CM32 cm32Protocol) {
        this.cm32Protocol = cm32Protocol;
    }

    public String getRegisterInfo() throws IOException {
        return cm32Protocol.getRegisterFactory().getRegisterInfo();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return null;
    }

}