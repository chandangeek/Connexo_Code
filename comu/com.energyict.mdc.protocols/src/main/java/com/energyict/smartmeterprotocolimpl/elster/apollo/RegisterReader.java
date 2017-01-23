package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.ActivePassive;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.PrivacyEnhancingDataAggregation;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 26-nov-2010
 * Time: 9:53:09
 */
public class RegisterReader {

    public static final ObisCode ActivityCalendarNameObisCode = ObisCode.fromString("0.0.13.0.1.255");
    public static final ObisCode ChangeOfSupplierNameObisCode = ObisCode.fromString("1.0.1.64.0.255");
    public static final ObisCode OwnPublicKeysObisCode = ObisCode.fromString("0.128.0.2.0.2");
    public static final ObisCode StandingChargeObisCode = ObisCode.fromString("0.0.0.61.2.255");

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
                if (register.getObisCode().equals(OwnPublicKeysObisCode)) {
                    ObisCode publicKeysObisCode = ProtocolTools.setObisCodeField(OwnPublicKeysObisCode, 5, (byte) 255);
                    PrivacyEnhancingDataAggregation privacyEnhancingDataAggregation = meterProtocol.getDlmsSession().getCosemObjectFactory().getPrivacyEnhancingDataAggregation(publicKeysObisCode);
                    Structure ownPublicKey = privacyEnhancingDataAggregation.getOwnPublicKey();

                    OctetString public_x = (OctetString) ownPublicKey.getDataType(0);
                    OctetString public_y = (OctetString) ownPublicKey.getDataType(1);
                    registerValue = new RegisterValue(OwnPublicKeysObisCode, public_x.stringValue() + "," + public_y.stringValue());
                } else if (register.getObisCode().equals(StandingChargeObisCode)) {
                    ActivePassive information = meterProtocol.getDlmsSession().getCosemObjectFactory().getActivePassive(register.getObisCode());
                    Unsigned32 value = information.getValue().getUnsigned32();
                    ScalerUnit scalerUnit = information.getScalerUnit();
                    if (scalerUnit.getUnitCode() == 10) {
                        scalerUnit = new ScalerUnit(scalerUnit.getScaler(), 255);
                    }
                    Quantity quantity;
                    try {
                        quantity = new Quantity(value.getValue(), scalerUnit.getEisUnit());
                    } catch (ApplicationException e) {  // The BasUnit code is not found (not yet present)
                        quantity = new Quantity(value.getValue(), Unit.get(255, scalerUnit.getScaler()));
                    }
                    registerValue = new RegisterValue(register, quantity);
                } else if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        Unit unit = su.getUnitCode() == 56 ? Unit.get(BaseUnit.PERCENT, su.getScaler()) : su.getEisUnit();    //Replace dlms % by our %
                        AbstractDataType value = registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute());
                        registerValue = new RegisterValue(register, new Quantity(value.toBigDecimal(), unit));
                    }
                } else if (this.registerMap.containsKey(register)) {
                    if (getClassId(register.getObisCode()).getClassId() == DLMSClassId.DATA.getClassId()) {
                        if (register.getObisCode().equals(AS300ObisCodeProvider.LastBillingResetTimeStamp)) {
                            Data data = meterProtocol.getObjectFactory().getData(register.getObisCode());
                            registerValue = new RegisterValue(register, data.getBillingDate().toString());
                        } else if (register.getObisCode().equals(AS300ObisCodeProvider.ActiveLongFirmwareIdentifierACOR)) {
                            registerValue = new RegisterValue(register, meterProtocol.getFirmwareVersion());
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
        if (register.getObisCode().equals(ActivityCalendarNameObisCode) || register.getObisCode().equals(ChangeOfSupplierNameObisCode)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(abstractDataType.getContentByteArray()));
        }
        return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()), null, null, null, new Date(), 0, String.valueOf(abstractDataType.longValue()));
    }


    public static DLMSClassId getClassId(ObisCode obisCode) {

        if (obisCode.getA() == 0 && obisCode.getB() == 0 && obisCode.getC() == 96 && obisCode.getD() == 1) {
            return DLMSClassId.DATA;
        }

        if (obisCode.getA() == 1 && obisCode.getB() == 0) {
            if (((obisCode.getC() >= 1) && (obisCode.getC() <= 8)) && ((obisCode.getD() == 8) || (obisCode.getD() == 25) || (obisCode.getD() == 29)) && (obisCode.getF() == 255)) {
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

            } else if (obisCode.equals(AS300ObisCodeProvider.NrOfVoltageSagsAvgVoltageObisCode) || obisCode.equals(AS300ObisCodeProvider.NrOfVoltageSwellsAvgVoltageObisCode)) {
                return DLMSClassId.DATA;
            } else if (obisCode.equals(AS300ObisCodeProvider.DurationVoltageSagsAvgVoltageObisCode) || obisCode.equals(AS300ObisCodeProvider.DurationVoltageSwellsAvgVoltageObisCode) || obisCode.equals(AS300ObisCodeProvider.RefVoltagePQObisCode)) {
                return DLMSClassId.REGISTER;
            }
        }
        if (obisCode.equals(AS300ObisCodeProvider.ActiveQuadrantObisCode)
                || obisCode.equals(AS300ObisCodeProvider.ActiveQuadrantL1ObisCode)
                || obisCode.equals(AS300ObisCodeProvider.ActiveQuadrantL2ObisCode)
                || obisCode.equals(AS300ObisCodeProvider.ActiveQuadrantL3ObisCode)
                || obisCode.equals(AS300ObisCodeProvider.PhasePrecense)
                || obisCode.equals(AS300ObisCodeProvider.TransformerRatioCurrentDenObisCode)
                || obisCode.equals(AS300ObisCodeProvider.TransformerRatioVoltageDenObisCode)
                || obisCode.equals(AS300ObisCodeProvider.TransformerRatioCurrentNumObisCode)
                || obisCode.equals(AS300ObisCodeProvider.TransformerRatioVoltageNumObisCode)) {
            return DLMSClassId.DATA;
        }
        if (obisCode.equals(AS300ObisCodeProvider.CurrentActiveRateContract1ObisCode)) {
            return DLMSClassId.DATA;
        } else if (obisCode.equals(AS300ObisCodeProvider.ActiveCalendarNameObisCode)) {
            return DLMSClassId.ACTIVITY_CALENDAR;
        } else if (obisCode.equals(AS300ObisCodeProvider.PassiveCalendarNameObisCode)) {
            return DLMSClassId.ACTIVITY_CALENDAR;
        }
        if (isData(obisCode)) {
            return DLMSClassId.DATA;
        }

        if (isBlockRegister(obisCode) || isBlockRegisterThreshold(obisCode)) {
            return DLMSClassId.REGISTER;
        }

        if (obisCode.equals(AS300ObisCodeProvider.LoadProfileDaily) || obisCode.equals(AS300ObisCodeProvider.LoadProfileMonthly) || obisCode.equals(AS300ObisCodeProvider.LoadProfileP1) || obisCode.equals(AS300ObisCodeProvider.LoadProfileBlockDaily) || obisCode.equals(AS300ObisCodeProvider.LoadProfileBlockMonthly)) {
            return DLMSClassId.PROFILE_GENERIC;
        }

        return DLMSClassId.UNKNOWN;
    }

    private static boolean isData(ObisCode obisCode) {
        return obisCode.equals(AS300ObisCodeProvider.ActiveLongFirmwareIdentifierACOR) || obisCode.equals(AS300ObisCodeProvider.ActiveLongFirmwareIdentifierMCOR) || obisCode.equals(AS300ObisCodeProvider.FirmwareVersionObisCode) || obisCode.equals(AS300ObisCodeProvider.FormerFirmwareVersionObisCode) || obisCode.equals(AS300ObisCodeProvider.E_OperationalFirmwareVersionObisCode) || obisCode.equals(AS300ObisCodeProvider.MIDCheckSumObisCode) || obisCode.equals(AS300ObisCodeProvider.ClockSynchronizationObisCode) || obisCode.equals(AS300ObisCodeProvider.clockSyncWindow) || obisCode.equals(AS300ObisCodeProvider.clockShiftInvalidLimit) || obisCode.equals(AS300ObisCodeProvider.clockShiftEventLimit) || obisCode.equals(AS300ObisCodeProvider.ReferenceTime) || obisCode.equals(AS300ObisCodeProvider.CurrentActiveRateContract1ObisCode) || obisCode.equals(AS300ObisCodeProvider.LastBillingResetTimeStamp) || obisCode.equals(AS300ObisCodeProvider.BillingResetLockoutTime) || obisCode.equals(AS300ObisCodeProvider.ErrorRegister) || obisCode.equals(AS300ObisCodeProvider.AlarmFilter) || obisCode.equals(AS300ObisCodeProvider.AlarmRegister) || obisCode.equals(AS300ObisCodeProvider.DaysSinceBillingReset);
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
                        DLMSAttribute registerUnit = new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute registerValue = new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                        ComposedRegister composedRegister = new ComposedRegister(registerValue, registerUnit);
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else {
                        this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                } else if (rObisCode.equals(ChangeOfSupplierNameObisCode)) {
                    this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, 9000));
                    dlmsAttributes.add(this.registerMap.get(register));
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }
}