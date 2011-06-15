package com.energyict.genericprotocolimpl.elster.AM100R.Apollo;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ActivityCalendarController;

import java.io.IOException;
import java.util.Map;

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

            } else if (obisCode.equals(ObisCodeProvider.NrOfVoltageSagsAvgVoltageObisCode)
                    || obisCode.equals(ObisCodeProvider.NrOfVoltageSwellsAvgVoltageObisCode)) {
                Data data = getMeterProtocol().getApolloObjectFactory().getData(obisCode);
                return new RegisterValue(obisCode, new Quantity(data.getString(), Unit.getUndefined()));
            } else if (obisCode.equals(ObisCodeProvider.DurationVoltageSagsAvgVoltageObisCode)
                    || obisCode.equals(ObisCodeProvider.DurationVoltageSwellsAvgVoltageObisCode)
                    || obisCode.equals(ObisCodeProvider.RefVoltagePQObisCode)) {
                Register register = getMeterProtocol().getApolloObjectFactory().getRegister(obisCode);
                return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
            }
        }

        // Other abstract Objects
        if (obisCode.equals(ObisCodeProvider.ActiveQuadrantObisCode)
                || obisCode.equals(ObisCodeProvider.ActiveQuadrantL1ObisCode)
                || obisCode.equals(ObisCodeProvider.ActiveQuadrantL2ObisCode)
                || obisCode.equals(ObisCodeProvider.ActiveQuadrantL3ObisCode)
                || obisCode.equals(ObisCodeProvider.PhasePrecense)
                || obisCode.equals(ObisCodeProvider.TransformerRatioCurrentDenObisCode)
                || obisCode.equals(ObisCodeProvider.TransformerRatioVoltageDenObisCode)
                || obisCode.equals(ObisCodeProvider.TransformerRatioCurrentNumObisCode)
                || obisCode.equals(ObisCodeProvider.TransformerRatioVoltageNumObisCode)) {
            Data data = getMeterProtocol().getApolloObjectFactory().getData(obisCode);
            return new RegisterValue(obisCode, new Quantity(data.getString(), Unit.getUndefined()));
        }

        /* ActivityCalendar related Objects */
        if (obisCode.equals(ObisCodeProvider.CurrentActiveRateContract1ObisCode)) {
            Data data = getMeterProtocol().getApolloObjectFactory().getData(obisCode);
            return new RegisterValue(obisCode, new Quantity(data.getValue(), Unit.getUndefined()));
        } else if (obisCode.equals(ObisCodeProvider.ActiveCalendarNameObisCode)) {
            ActivityCalendarController acc = getMeterProtocol().getActivityCalendarController();
            return new RegisterValue(obisCode, acc.getCalendarName());
        } else if (obisCode.equals(ObisCodeProvider.PassiveCalendarNameObisCode)) {
            ActivityCalendar ac = getMeterProtocol().getApolloObjectFactory().getActivityCalendar();
            return new RegisterValue(obisCode, ac.readCalendarNamePassive().stringValue());
        }

        /* FirmwareVersion related objects */
        if (obisCode.equals(ObisCodeProvider.ActiveLongFirmwareIdentifierACOR)) {
            String acor = getMeterProtocol().getApolloObjectFactory().getActiveFirmwareIdACOR().getAttrbAbstractDataType(-1).getOctetString().stringValue();
            return new RegisterValue(obisCode, "ACOR : " + acor);
        } else if (obisCode.equals(ObisCodeProvider.ActiveLongFirmwareIdentifierMCOR)) {
            String mcor = getMeterProtocol().getApolloObjectFactory().getActiveFirmwareIdMCOR().getAttrbAbstractDataType(-1).getOctetString().stringValue();
            return new RegisterValue(obisCode, "MCOR : " + mcor);
        }

        /* TOU BlockRegisters */
        if (isBlockRegister(obisCode) || isBlockRegisterThreshold(obisCode)) {
            Register register = getMeterProtocol().getApolloObjectFactory().getRegister(obisCode);
            return new RegisterValue(obisCode, ParseUtils.registerToQuantity(register));
        }

        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
    }

    /**
     * Check if the given ObisCode is a BlockRegister ObisCode
     *
     * @param oc the ObisCode to check
     * @return true if it is a blockRegister ObisCode, false otherwise
     */
    private boolean isBlockRegister(final ObisCode oc) {
        return (oc.getA() == 1) && ((oc.getD() == 8) || (oc.getD() == 9)) && (oc.getF() == 255) &&
                ((oc.getB() >= 11) && (oc.getB() <= 18)) &&
                ((oc.getC() == 1) || (oc.getC() == 2)) &&
                ((oc.getE() >= 0) && (oc.getE() <= 8));
    }

    /**
     * Check if the given ObisCode is a BlockRegisterThreshold ObisCode.
     *
     * @param oc the ObisCode to check
     * @return true if it is a blockRegisterThreshold ObisCode, false otherwise
     */
    private boolean isBlockRegisterThreshold(final ObisCode oc) {
        return (oc.getA() == 1) && (oc.getB() == 0) &&
                ((oc.getC() == 1) || oc.getC() == 2) &&
                (oc.getD() == 60) && (oc.getE() >= 1) && (oc.getE() <= 7) && (oc.getF() == 255);
    }
}
