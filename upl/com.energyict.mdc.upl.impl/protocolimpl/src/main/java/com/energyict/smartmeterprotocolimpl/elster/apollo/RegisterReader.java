package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocol.*;
import com.energyict.smartmeterprotocolimpl.elster.apollo.composed.ComposedRegister;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 26-nov-2010
 * Time: 9:53:09
 */
public class RegisterReader {

    private final AS300 meterProtocol;

    public RegisterReader(AS300 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<Register, ComposedRegister>();
    private Map<Register, DLMSAttribute> registerMap = new HashMap<Register, DLMSAttribute>();

    public List<RegisterValue> read(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(registers, this.meterProtocol.getProperties().isBulkRequest());

        for (Register register : registers) {
            RegisterValue registerValue = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        Unit unit = su.getUnitCode() == 56 ? Unit.get(BaseUnit.PERCENT, su.getScaler()) : su.getUnit();    //Replace dlms % by our %
                        AbstractDataType value = registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute());
                        registerValue = new RegisterValue(register, new Quantity(value.toBigDecimal(), unit));
                    }
                } else if (this.registerMap.containsKey(register)) {
                    if (getClassId(register.getObisCode()).getClassId() == DLMSClassId.DATA.getClassId()) {
                        if (register.getObisCode().equals(ObisCodeProvider.LastBillingResetTimeStamp)) {
                            Data data = meterProtocol.getObjectFactory().getData(register.getObisCode());
                            registerValue = new RegisterValue(register, data.getBillingDate().toString());
                        } else {
                            try {
                                registerValue = new RegisterValue(register, meterProtocol.getObjectFactory().getData(register.getObisCode()).getString());
                            } catch (IOException e) {
                                registerValue = new RegisterValue(register, meterProtocol.getObjectFactory().getData(register.getObisCode()).getQuantityValue());
                            }
                        }
                    } else {
                        registerValue = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                    }
                }
            } catch (IOException e) {
                this.meterProtocol.getLogger().log(Level.WARNING, "Failed to fetch register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "]");
            }
            if (registerValue != null) {
                registerValues.add(registerValue);
            }
        }
        return registerValues;
    }


    private RegisterValue convertCustomAbstractObjectsToRegisterValues(Register register, AbstractDataType abstractDataType) throws UnsupportedException {
        return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()), null, null, null, new Date(), 0, String.valueOf(abstractDataType.longValue()));
    }


    public static DLMSClassId getClassId(ObisCode obisCode) {

        if (obisCode.getA() == 0 && obisCode.getB() == 0 && obisCode.getC() == 96 && obisCode.getD() == 1) {
            return DLMSClassId.DATA;
        }

        if (obisCode.getA() == 1 && obisCode.getB() == 0) {
            if (((obisCode.getC() >= 1) && (obisCode.getC() <= 8)) && ((obisCode.getD() == 8) || (obisCode.getD() == 29)) && (obisCode.getF() == 255)) {
                return DLMSClassId.REGISTER;
            }
            if (((obisCode.getC() == 1) || (obisCode.getC() == 2) || (obisCode.getC() == 3) || (obisCode.getC() == 4) || (obisCode.getC() == 9) || (obisCode.getC() == 10)) && (obisCode.getD() == 4 || obisCode.getD() == 25) && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
                return DLMSClassId.DEMAND_REGISTER;
            } else if ((obisCode.getA() == 1) && (obisCode.getB() == 0) && ((obisCode.getC() == 1) || (obisCode.getC() == 2) || (obisCode.getC() == 3) || (obisCode.getC() == 4)) && (obisCode.getD() == 6) && (obisCode.getF() == 255)) {
                return DLMSClassId.EXTENDED_REGISTER;
            }

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
                return DLMSClassId.REGISTER;
            }

            if (((obisCode.getC() == 12) || (obisCode.getC() == 32) || (obisCode.getC() == 52) || (obisCode.getC() == 72)) && (obisCode.getE() == 0) && (obisCode.getF() == 255)) {
                if ((obisCode.getD() == 43) || (obisCode.getD() == 31) || (obisCode.getD() == 33) || (obisCode.getD() == 44) || (obisCode.getD() == 35) || (obisCode.getD() == 37)) {
                    return DLMSClassId.REGISTER;
                } else if ((obisCode.getD() == 32) || (obisCode.getD() == 36)) {
                    return DLMSClassId.DATA;
                }

            } else if (obisCode.equals(ObisCodeProvider.NrOfVoltageSagsAvgVoltageObisCode) || obisCode.equals(ObisCodeProvider.NrOfVoltageSwellsAvgVoltageObisCode)) {
                return DLMSClassId.DATA;
            } else if (obisCode.equals(ObisCodeProvider.DurationVoltageSagsAvgVoltageObisCode) || obisCode.equals(ObisCodeProvider.DurationVoltageSwellsAvgVoltageObisCode) || obisCode.equals(ObisCodeProvider.RefVoltagePQObisCode)) {
                return DLMSClassId.REGISTER;
            }
        }
        if (obisCode.equals(ObisCodeProvider.ActiveQuadrantObisCode)
                || obisCode.equals(ObisCodeProvider.ActiveQuadrantL1ObisCode)
                || obisCode.equals(ObisCodeProvider.ActiveQuadrantL2ObisCode)
                || obisCode.equals(ObisCodeProvider.ActiveQuadrantL3ObisCode)
                || obisCode.equals(ObisCodeProvider.PhasePrecense)
                || obisCode.equals(ObisCodeProvider.TransformerRatioCurrentDenObisCode)
                || obisCode.equals(ObisCodeProvider.TransformerRatioVoltageDenObisCode)
                || obisCode.equals(ObisCodeProvider.TransformerRatioCurrentNumObisCode)
                || obisCode.equals(ObisCodeProvider.TransformerRatioVoltageNumObisCode)) {
            return DLMSClassId.DATA;
        }
        if (obisCode.equals(ObisCodeProvider.CurrentActiveRateContract1ObisCode)) {
            return DLMSClassId.DATA;
        } else if (obisCode.equals(ObisCodeProvider.ActiveCalendarNameObisCode)) {
            return DLMSClassId.ACTIVITY_CALENDAR;
        } else if (obisCode.equals(ObisCodeProvider.PassiveCalendarNameObisCode)) {
            return DLMSClassId.ACTIVITY_CALENDAR;
        }
        if (isData(obisCode)) {
            return DLMSClassId.DATA;
        }

        if (isBlockRegister(obisCode) || isBlockRegisterThreshold(obisCode)) {
            return DLMSClassId.REGISTER;
        }

        if (obisCode.equals(ObisCodeProvider.LoadProfileDaily) || obisCode.equals(ObisCodeProvider.LoadProfileMonthly) || obisCode.equals(ObisCodeProvider.LoadProfileP1) || obisCode.equals(ObisCodeProvider.LoadProfileBlockDaily) || obisCode.equals(ObisCodeProvider.LoadProfileBlockMonthly)) {
            return DLMSClassId.PROFILE_GENERIC;
        }

        return DLMSClassId.UNKNOWN;
    }

    private static boolean isData(ObisCode obisCode) {
        return obisCode.equals(ObisCodeProvider.ActiveLongFirmwareIdentifierACOR) || obisCode.equals(ObisCodeProvider.ActiveLongFirmwareIdentifierMCOR) || obisCode.equals(ObisCodeProvider.FirmwareVersionObisCode) || obisCode.equals(ObisCodeProvider.FormerFirmwareVersionObisCode) || obisCode.equals(ObisCodeProvider.E_OperationalFirmwareVersionObisCode) || obisCode.equals(ObisCodeProvider.MIDCheckSumObisCode) || obisCode.equals(ObisCodeProvider.ClockSynchronizationObisCode) || obisCode.equals(ObisCodeProvider.clockSyncWindow) || obisCode.equals(ObisCodeProvider.clockShiftInvalidLimit) || obisCode.equals(ObisCodeProvider.clockShiftEventLimit) || obisCode.equals(ObisCodeProvider.ReferenceTime) || obisCode.equals(ObisCodeProvider.CurrentActiveRateContract1ObisCode) || obisCode.equals(ObisCodeProvider.LastBillingResetTimeStamp) || obisCode.equals(ObisCodeProvider.BillingResetLockoutTime) || obisCode.equals(ObisCodeProvider.ErrorRegister) || obisCode.equals(ObisCodeProvider.AlarmFilter) || obisCode.equals(ObisCodeProvider.AlarmRegister) || obisCode.equals(ObisCodeProvider.DaysSinceBillingReset);
    }

    /**
     * Check if the given ObisCode is a BlockRegister ObisCode
     *
     * @param oc the ObisCode to check
     * @return true if it is a blockRegister ObisCode, false otherwise
     */
    private static boolean isBlockRegister(final ObisCode oc) {
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
    private static boolean isBlockRegisterThreshold(final ObisCode oc) {
        return (oc.getA() == 1) && (oc.getB() == 0) &&
                ((oc.getC() == 1) || oc.getC() == 2) &&
                (oc.getD() == 60) && (oc.getE() >= 1) && (oc.getE() <= 7) && (oc.getF() == 255);
    }

    private ComposedCosemObject constructComposedObjectFromRegisterList(List<Register> registers, boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = register.getObisCode();
                UniversalObject uo = new UniversalObject(rObisCode.getLN(), getClassId(rObisCode).getClassId(), 0);
                if (uo.getClassID() != DLMSClassId.UNKNOWN.getClassId()) {
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, RegisterAttributes.Register_Unit.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute registerValue = new DLMSAttribute(rObisCode, RegisterAttributes.Register_Value.getAttributeNumber(), uo.getClassID());
                        ComposedRegister composedRegister = new ComposedRegister(registerValue, registerUnit);
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else {
                        this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }
}