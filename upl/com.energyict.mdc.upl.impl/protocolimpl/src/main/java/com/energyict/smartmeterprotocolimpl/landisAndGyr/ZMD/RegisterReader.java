package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.protocol.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 8:59
 */
public class RegisterReader {


    private final ZMD meterProtocol;

    public RegisterReader(ZMD meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<RegisterValue> read(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        for (Register register : registers) {
            try {
                ObisCodeMapper ocm = new ObisCodeMapper(meterProtocol.getCosemObjectFactory(), meterProtocol.getMeterConfig(), meterProtocol);
                registerValues.add(ocm.getRegisterValue(register));
            } catch (IOException e) {
                throw new NoSuchRegisterException("Problems while reading register " + register.getObisCode().toString() + ": " + e.getMessage());
            }
        }
        return registerValues;
    }
}