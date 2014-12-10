package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 20/01/12
 * Time: 10:48
 */
public class RegisterReader {

    private final IskraMx372 meterProtocol;
    private ObisCodeMapper ocm;

    public RegisterReader(IskraMx372 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<RegisterValue> read(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        for (Register register : registers) {
            try {
                registerValues.add(getObisCodeMapper().getRegisterValue(register));
            } catch (IOException e) {
                throw new NoSuchRegisterException("Problems while reading register " + register.getObisCode().toString() + ": " + e.getMessage());
            }
        }
        return registerValues;
    }

    public ObisCodeMapper getObisCodeMapper() {
        if (ocm == null) {
            ocm = new ObisCodeMapper(meterProtocol.getCosemObjectFactory(), meterProtocol);
        }
        return ocm;
    }
}
