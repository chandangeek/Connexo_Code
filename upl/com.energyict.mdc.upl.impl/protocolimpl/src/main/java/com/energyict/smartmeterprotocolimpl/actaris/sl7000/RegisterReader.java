package com.energyict.smartmeterprotocolimpl.actaris.sl7000;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 */
public class RegisterReader {

    private final ActarisSl7000 meterProtocol;
    private ObisCodeMapper obisCodeMapper;

    public RegisterReader(ActarisSl7000 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<>();

        // Loop over all registers to determine the highest billingPoint & request billingPointDateTime for highest point
        // This will ensure all necessary billing points are present in the  profile buffer
        int billingPoint = -1;
        for (Register reg : registers) {
            int f = reg.getObisCode().getF();
            if (f != 255 && f > billingPoint) {
                billingPoint = f;
            }
        }

        try {
            if (billingPoint != -1 && billingPoint <= meterProtocol.getStoredValues().getBillingPointCounter()) {
                meterProtocol.getStoredValues().getBillingPointTimeDate(billingPoint);
            }
        } catch (IOException e) {
            meterProtocol.getLogger().log(Level.SEVERE, "Problems while reading historical billingPoint " + billingPoint);
        }

        for (Register register : registers) {
            try {
                registerValues.add(getObisCodeMapper().getRegisterValue(register));
            } catch (NestedIOException e) {
                if (ProtocolTools.getRootCause(e) instanceof ConnectionException || ProtocolTools.getRootCause(e) instanceof DLMSConnectionException) {
                    throw e;    // In case of a connection exception (of which we cannot recover), do throw the error.
                }
                meterProtocol.getLogger().log(Level.SEVERE, "Problems while reading register " + register.getObisCode().toString() + (e.getMessage() != null ? (": " + e.getMessage()) : ""));
            } catch (IOException e) {
                meterProtocol.getLogger().log(Level.SEVERE, "Problems while reading register " + register.getObisCode().toString() + (e.getMessage() != null ? (": " + e.getMessage()) : ""));
            }
        }
        return registerValues;
    }

    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(meterProtocol);
        }
        return obisCodeMapper;
    }
}
