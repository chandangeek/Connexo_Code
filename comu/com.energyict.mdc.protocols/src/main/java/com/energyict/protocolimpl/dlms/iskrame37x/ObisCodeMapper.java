package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.RegisterMonitor;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NotInObjectListException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    static ObisCode billingProfileObiscodeMonthly = ObisCode.fromString("1.0.98.1.0.255");
    static ObisCode billingProfileObiscodeDaily = ObisCode.fromString("1.0.98.2.0.255");

    IskraME37X meterProtocol;
    CosemObjectFactory cof;
    StoredValuesImpl[] storedValues;

    public ObisCodeMapper(IskraME37X meterProtocol) throws IOException {
        this.meterProtocol = meterProtocol;
        this.cof = meterProtocol.getCosemObjectFactory();
        storedValues = new StoredValuesImpl[]{new StoredValuesImpl(cof, billingProfileObiscodeMonthly), new StoredValuesImpl(cof, billingProfileObiscodeDaily)};
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode);
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {

        RegisterValue registerValue;
        try {
            // *********************************************************************************
            //Billing registers
            if (obisCode.getF() != 255) {
                int absBillingPoint = Math.abs(obisCode.getF());
                int billingPoint = absBillingPoint > 11 ? absBillingPoint - 12 : absBillingPoint;

                try {
                    if ((obisCode.toString().contains("1.0.0.1.2.")) || (obisCode.toString().contains("1.1.0.1.2."))) { // billing point timestamp
                        Date billingPointTimeDate = getStoredValues(obisCode).getBillingPointTimeDate(billingPoint);
                        registerValue = new RegisterValue(obisCode, billingPointTimeDate);
                        return registerValue;
                    } else {
                        ObisCode obis = ProtocolTools.setObisCodeField(obisCode, 5, (byte) billingPoint);
                        HistoricalValue historicalValue = getStoredValues(obisCode).getHistoricalValue(obis);
                        return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
                    }
                } catch (NoSuchRegisterException e) {
                    meterProtocol.getLogger().warning(e.getMessage());
                }
            }
            // ---------------------------------------------------------------------------------


            // *********************************************************************************
            // General purpose ObisRegisters & abstract general service
            if ((obisCode.toString().contains("1.1.0.1.0.255")) || (obisCode.toString().contains("1.0.0.1.0.255"))) { // billing counter
                Data data = cof.getData(new ObisCode(1, 0, 0, 1, 0, 255));
                Unsigned16 counter = (Unsigned16) data.getValueAttr();
                registerValue = new RegisterValue(obisCode, new Quantity(counter.toBigDecimal(), Unit.getUndefined()));
                return registerValue;
            } // billing counter

            // *********************************************************************************
            if (obisCode.toString().contains("1.0.0.1.1.255")) { // nr of available monthly billing periods
                int counter = storedValues[0].getBillingPointCounter();
                registerValue = new RegisterValue(obisCode, new Quantity(counter, Unit.getUndefined()));
                return registerValue;
            } // billing counter

            if (obisCode.toString().contains("1.1.0.1.1.255")) { // nr of available daily billing periods
                int counter = storedValues[1].getBillingPointCounter();
                registerValue = new RegisterValue(obisCode, new Quantity(counter, Unit.getUndefined()));
                return registerValue;
            } // billing counter

            // *********************************************************************************
            // Electricity related ObisRegisters
            if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() >= 2))) {
                if (obisCode.getD() == 8) { // cumulative values, indexes
                    Register register = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 8) { // cumulative values, indexes
                else if (obisCode.getD() == 4) { // current average
                    Register register = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 4) { // current average
                else if (obisCode.getD() == 5) { // last average
                    Register register = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 5) { // last average
                else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister register = cof.getExtendedRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()), register.getCaptureTime());
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {

            if (!((obisCode.getA() == 0) && (obisCode.getB() == 0)) && obisCode.getC() == 128) {
                if ((obisCode.getD() == 50) && (obisCode.getE() == 0)) {
                    ExtendedRegister register = cof.getExtendedRegister(obisCode);
                    BigDecimal am = BigDecimal.valueOf(register.getValue());
                    Unit u;
                    if (register.getScalerUnit().getUnitCode() != 0) {
                        u = register.getScalerUnit().getEisUnit();
                    } else {
                        u = Unit.get(BaseUnit.UNITLESS, 0);
                    }

                    Date captime = register.getCaptureTime();
                    return new RegisterValue(obisCode, new Quantity(am, u), isValidCaptureTime(captime) ? captime : null);
                }
            }

        } catch (IOException e) {
        }

        // *********************************************************************************
        // If the register is not yet read, try to read it out in a generic way
        if (obisCode.getF() == 255) {
            final UniversalObject uo = findObjectInMeterConfig(obisCode);
            if (uo != null) {
                if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                    com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, cosemRegister.getQuantityValue());
                } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                    final DemandRegister demandRegister = cof.getDemandRegister(obisCode);
                    Date captureTime = demandRegister.getCaptureTime();
                    return new RegisterValue(obisCode, demandRegister.getQuantityValue(), isValidCaptureTime(captureTime) ? captureTime : null);
                } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                    final ExtendedRegister extendedRegister = cof.getExtendedRegister(obisCode);
                    Date captureTime = extendedRegister.getCaptureTime();
                    return new RegisterValue(obisCode, extendedRegister.getQuantityValue(), isValidCaptureTime(captureTime) ? captureTime : null);
                } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                    final Data data = cof.getData(obisCode);
                    VisibleString visibleString = data.getValueAttr().getVisibleString();
                    if (visibleString != null && visibleString.getStr() != null) {
                        return new RegisterValue(obisCode, visibleString.getStr());
                    }
                    OctetString octetString = data.getValueAttr().getOctetString();
                    if (octetString != null && octetString.stringValue() != null) {
                        return new RegisterValue(obisCode, octetString.stringValue());
                    }
                    long longValue = data.getValueAttr().longValue();
                    if (longValue != -1) {
                        return new RegisterValue(obisCode, new Quantity(longValue, Unit.getUndefined()));
                    }
                } else if (uo.getClassID() == DLMSClassId.REGISTER_MONITOR.getClassId()) {
                    RegisterMonitor registerMonitor = cof.getRegisterMonitor(obisCode);
                    int value = registerMonitor.readThresholds().getDataType(0).intValue();
                    return new RegisterValue(obisCode, new Quantity(value, Unit.getUndefined()));
                }
            }
        }
        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");

    } // private Object doGetRegister(ObisCode obisCode) throws IOException

    private UniversalObject findObjectInMeterConfig(ObisCode obisCode) throws IOException {
        try {
            return meterProtocol.getMeterConfig().findObject(obisCode);
        } catch (NotInObjectListException e) {
            meterProtocol.getLogger().severe(obisCode.toString() + " not found in meter's instantiated object list!");
            throw e;
        }
    }

    /**
     * Check if the given capture date is valid.
     * Basically this checks if the date is not equal to 01 Jan 00:00:00:1970
     *
     * @param captureTime The capture date
     * @return true, if the captureTime is valid
     *         false, if the captureTime is not valid
     */
    private boolean isValidCaptureTime(Date captureTime) {
        Calendar cleanCalendar = ProtocolUtils.getCleanCalendar(meterProtocol.getTimeZone());   // Thu Jan 01 00:00:00 1970, device time zone
        return captureTime.after(cleanCalendar.getTime());
    }

    private StoredValuesImpl getStoredValues(ObisCode obisCode) {
        if (Math.abs(obisCode.getF()) < 12) {
            return storedValues[0];
        } else {
            return storedValues[1];
        }
    }

}