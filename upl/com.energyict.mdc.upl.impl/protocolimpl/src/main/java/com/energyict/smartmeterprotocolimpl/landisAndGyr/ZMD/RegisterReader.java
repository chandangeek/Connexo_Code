package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

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
            } catch (NestedIOException e) {
                if (ProtocolTools.getRootCause(e) instanceof ConnectionException || ProtocolTools.getRootCause(e) instanceof DLMSConnectionException) {
                    throw e;    // In case of a connection exception (of which we cannot recover), do throw the error.
                }
                meterProtocol.getLogger().log(Level.SEVERE, "Problems while reading register " + register.getObisCode().toString() + ": " + e.getMessage());
            } catch (IOException e) {
                meterProtocol.getLogger().log(Level.SEVERE, "Problems while reading register " + register.getObisCode().toString() + ": " + e.getMessage());
            }
        }
        return registerValues;
    }
}