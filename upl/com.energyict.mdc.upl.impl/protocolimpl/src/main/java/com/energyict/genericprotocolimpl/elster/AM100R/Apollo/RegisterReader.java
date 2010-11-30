package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 26-nov-2010
 * Time: 9:53:09
 */
public class RegisterReader {

    private final ApolloMeter meterProtocol;

    /**
     * Default constructor
     *
     * @param meter the ApolloMeter protocol
     */
    public RegisterReader(ApolloMeter meter) {
        this.meterProtocol = meter;
    }

    /**
     * Read the registers defined in the {@link com.energyict.mdw.core.CommunicationProfile}.
     * TODO check how you can implement the readBulkRegisters
     */
    public Map<RtuRegister, RegisterValue> readRegisters() throws IOException {
        return getMeterProtocol().doReadRegisters();
    }

    /**
     * Getter for the ApolloMeter
     *
     * @return the ApolloMeter
     */
    private ApolloMeter getMeterProtocol() {
        return this.meterProtocol;
    }

    /**
     * Read the RegisterValue from the Device
     *
     * @param obisCode the {@link com.energyict.obis.ObisCode} to read from the Device
     * @return the value of that Register
     * @throws IOException when something happened during the read
     */
    public RegisterValue read(ObisCode obisCode) throws IOException {

        //TODO do the instantaneous values, they are in a ProfileObject 0.0.21.0.6.255

        //Electricity related ObisRegisters

        // 1/ Active Energy Import/Export - Reactive Energy Q1/Q2/Q3/Q4 with different rates
        if ((obisCode.getA() == 1)
                && (obisCode.getB() == 0)
                && ((obisCode.getC() == 1) || (obisCode.getC() == 2) || ((obisCode.getC() >= 5) && (obisCode.getC() <= 8)))
                && ((obisCode.getD() == 8) || (obisCode.getD() == 29))
                && ((obisCode.getE() == 0)
                || ((obisCode.getE() >= 10) && (obisCode.getE() <= 16))
                || ((obisCode.getE() >= 20) && (obisCode.getE() <= 26))
                || ((obisCode.getE() >= 30) && (obisCode.getE() <= 36)))
                && (obisCode.getF() == 255)) {
            Register register = getMeterProtocol().getApolloObjectFactory().getRegister(obisCode);
            return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
        }

        // 2/ Average Demand Registers && Maximum Demand Registers
        if ((obisCode.getA() == 1)
                && (obisCode.getB() == 0)
                && ((obisCode.getC() == 1) || (obisCode.getC() == 2))
                && (obisCode.getD() == 4)
                && (obisCode.getE() == 0)
                && (obisCode.getF() == 255)) {
            DemandRegister dRegister = getMeterProtocol().getApolloObjectFactory().getDemandRegister(obisCode);
            return new RegisterValue(obisCode, ParseUtils.cosemObjectToQuantity(dRegister));
        } else if ((obisCode.getA() == 1)
                && (obisCode.getB() == 0)
                && ((obisCode.getC() == 1) || (obisCode.getC() == 2))
                && (obisCode.getD() == 6)
                && (((obisCode.getE() >= 10) && (obisCode.getE() <= 16))
                || ((obisCode.getE() >= 20) && (obisCode.getE() <= 26))
                || ((obisCode.getE() >= 30) && (obisCode.getE() <= 36)))
                && (obisCode.getF() == 255)) {
            ExtendedRegister dRegister = getMeterProtocol().getApolloObjectFactory().getExtendedRegister(obisCode);
            return new RegisterValue(obisCode, ParseUtils.cosemObjectToQuantity(dRegister));
        }

        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
    }


}
