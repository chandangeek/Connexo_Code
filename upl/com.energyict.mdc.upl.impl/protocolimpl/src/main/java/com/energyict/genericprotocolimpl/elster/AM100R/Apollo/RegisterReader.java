package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;

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

        //Electricity related ObisRegisters
        if (obisCode.getA() == 1 && obisCode.getB() == 0) {
            // 1/ Active Energy Import/Export - Reactive Energy Q1/Q2/Q3/Q4 with different rates
            if (((obisCode.getC() == 1) || (obisCode.getC() == 2) || ((obisCode.getC() >= 5) && (obisCode.getC() <= 8)))
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
            if (((obisCode.getC() == 1) || (obisCode.getC() == 2))
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

            // 3/ Instantaneous values
            if (((obisCode.getC() == 1) || (obisCode.getC() == 2) || (obisCode.getC() == 3) || (obisCode.getC() == 4)
                    || (obisCode.getC() == 31) || (obisCode.getC() == 32)
                    || (obisCode.getC() == 51) || (obisCode.getC() == 52)
                    || (obisCode.getC() == 71) || (obisCode.getC() == 72)
                    || (obisCode.getC() == 90)
                    || (obisCode.getC() == 21) || (obisCode.getC() == 22) || (obisCode.getC() == 23) || (obisCode.getC() == 24)
                    || (obisCode.getC() == 41) || (obisCode.getC() == 42) || (obisCode.getC() == 43) || (obisCode.getC() == 44)
                    || (obisCode.getC() == 61) || (obisCode.getC() == 62) || (obisCode.getC() == 63) || (obisCode.getC() == 64)
                    || (obisCode.getC() == 13) || (obisCode.getC() == 33) || (obisCode.getC() == 53) || (obisCode.getC() == 73))
                    && (obisCode.getD() == 7)   // D equals 7 means instantaneous
                    && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
                Register register = getMeterProtocol().getApolloObjectFactory().getRegister(obisCode);
                return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
            }

            // 4/ PowerQualities Registers && PowerQualities Numbers
            if (((obisCode.getC() == 12) || (obisCode.getC() == 32) || (obisCode.getC() == 52) || (obisCode.getC() == 72))
                    && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
                if ((obisCode.getD() == 43) || (obisCode.getD() == 31) || (obisCode.getD() == 33)
                        || (obisCode.getD() == 44) || (obisCode.getD() == 35) || (obisCode.getD() == 37)) {
                    Register register = getMeterProtocol().getApolloObjectFactory().getRegister(obisCode);
                    return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
                } else if ((obisCode.getD() == 32) || (obisCode.getD() == 36)) {
                    Data data = getMeterProtocol().getApolloObjectFactory().getData(obisCode);
                    return new RegisterValue(obisCode, new Quantity(data.getString(), Unit.getUndefined()));
                }

            } else if (obisCode.equals(ObisCodeProvider.nrOfVoltageSagsAvgVoltageObisCode)
                    || obisCode.equals(ObisCodeProvider.nrOfVoltageSwellsAvgVoltageObisCode)) {
                Data data = getMeterProtocol().getApolloObjectFactory().getData(obisCode);
                return new RegisterValue(obisCode, new Quantity(data.getString(), Unit.getUndefined()));
            } else if (obisCode.equals(ObisCodeProvider.durationVoltageSagsAvgVoltageObisCode)
                    || obisCode.equals(ObisCodeProvider.durationVoltageSwellsAvgVoltageObisCode)
                    || obisCode.equals(ObisCodeProvider.refVoltagePQObisCode)) {
                Register register = getMeterProtocol().getApolloObjectFactory().getRegister(obisCode);
                return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
            }
        }

        // Other abstract Objects
        if (obisCode.equals(ObisCodeProvider.activeQuadrantObisCode)
                || obisCode.equals(ObisCodeProvider.activeQuadrantL1ObisCode)
                || obisCode.equals(ObisCodeProvider.activeQuadrantL2ObisCode)
                || obisCode.equals(ObisCodeProvider.activeQuadrantL3ObisCode)
                || obisCode.equals(ObisCodeProvider.phasePrecense)
                || obisCode.equals(ObisCodeProvider.transformerRatioCurrentDenObisCode)
                || obisCode.equals(ObisCodeProvider.transformerRatioVoltageDenObisCode)
                || obisCode.equals(ObisCodeProvider.transformerRatioCurrentNumObisCode)
                || obisCode.equals(ObisCodeProvider.transformerRatioVoltageNumObisCode)) {
            Data data = getMeterProtocol().getApolloObjectFactory().getData(obisCode);
            return new RegisterValue(obisCode, new Quantity(data.getString(), Unit.getUndefined()));
        }


        //TODO check if you need this one? THe instantaneous values are already available in the above if/else structure
//        if ((obisCode.getA() == 1)
//                && (obisCode.getB() == 0)
//                && ((obisCode.getC() == 1) || (obisCode.getC() == 2) || ((obisCode.getC() >= 5) && (obisCode.getC() <= 8)))
//                && (obisCode.getD() == 7)   // D equals 7 means instantaneous
//                && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
//            ProfileGeneric instantEnergyProfile = getMeterProtocol().getApolloObjectFactory().getInstantaneousEnergyProfile();
//            ObisCode tempOc = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), 8, 0, 255);
//            int index = getProfileIndex(instantEnergyProfile, tempOc);
//
//            if(index != -1){
//                DataContainer dc = instantEnergyProfile.getBuffer();
//                Quantity q = new Quantity(dc.getRoot().getStructure(0).getStructure(1).getInteger(index),
//                        getMeterProtocol().getApolloObjectFactory().getRegister(tempOc).getScalerUnit().getUnit());
//                return new RegisterValue(obisCode, q);
//            } else {
//                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
//            }
//
//        }

        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
    }

    /**
     * Get the index of the profile
     *
     * @param instantEnergyProfile
     * @param obisCode
     * @return
     * @throws IOException
     */
    private int getProfileIndex(ProfileGeneric instantEnergyProfile, ObisCode obisCode) throws IOException {
        int index = 0;
        for (CapturedObject co : instantEnergyProfile.getCaptureObjects()) {
            if (ProfileUtils.isChannelData(co)) {
                if (co.getLogicalName().getObisCode().equals(obisCode)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }


}
