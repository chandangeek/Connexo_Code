package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.RegisterMonitor;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.generic.ParseUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 10:29
 */
public class ObisCodeMapper {

    private final CosemObjectFactory cof;
    private final DLMSMeterConfig meterConfig;
    private final ZMD protocol;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public ObisCodeMapper(final CosemObjectFactory cof, final DLMSMeterConfig meterConfig, final ZMD protocol) {
        this.cof = cof;
        this.meterConfig = meterConfig;
        this.protocol = protocol;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(Register register) throws IOException {
        return (RegisterValue) doGetRegister(register);
    }

    private Object doGetRegister(Register register) throws IOException {

        RegisterValue registerValue = null;
        ObisCode obisCode = register.getObisCode();
        int billingPoint = -1;

        // obis F code
        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
            billingPoint = obisCode.getF();
        } else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
            billingPoint = obisCode.getF() * -1;
        } else if (obisCode.getF() == 255) {
            billingPoint = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
        if ((obisCode.toString().indexOf("1.0.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.1.0.1.0.255") != -1)) { // billing counter
            registerValue = new RegisterValue(register, cof.getCosemObject(ObisCode.fromString("1.0.0.1.0.255")).getQuantityValue());
            return registerValue;
        } // billing counter
        else if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                registerValue = new RegisterValue(register,
                        cof.getStoredValues().getBillingPointTimeDate(billingPoint));
                return registerValue;
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) {  // but we will read the 1.0.0.0.0.255   -   SerialNumber
            return new RegisterValue(register, this.protocol.getSerialNumber());
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.5.255"))) {  // but we will read the 1.0.0.2.0.255   -   FirmwareVersion
            return new RegisterValue(register, this.protocol.getFirmwareVersion());
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.4.255"))) {  // but we will read the 0.0.96.2.0.255  -   Program Counter
            return new RegisterValue(register, new Quantity(new BigInteger(String.valueOf(this.protocol.requestConfigurationProgramChanges())), Unit.getUndefined()));
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {    // Battery usage counter
            com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
            return new RegisterValue(register, ParseUtils.registerToQuantity(cosemRegister));
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.3.255"))) {    // Battery voltage
            com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
            return new RegisterValue(register, ParseUtils.registerToQuantity(cosemRegister));
        } else if (obisCode.equals(ObisCode.fromString("0.0.13.0.0.255"))) {    // Activity Calendar Name
            return new RegisterValue(register, null, null, null, null, new Date(), 0,
                    new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().getOctetStr()));
        } else if (obisCode.equals(ObisCode.fromString("0.0.97.97.0.255"))) {   // Error status
            com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
            String errorRegister = ProtocolUtils.outputHexString(cosemRegister.getValueAttr().getOctetString().getOctetStr());
            return new RegisterValue(register, errorRegister);
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.2.1.255"))) {    // Date and time of last configuration
            Data data = protocol.getCosemObjectFactory().getData(obisCode);
            OctetString valueAttr = (OctetString) data.getValueAttr();
            Date eventTime = getDateTime(valueAttr.getBEREncodedByteArray());
            return new RegisterValue(register, eventTime);
        } else if (obisCode.equals(ObisCode.fromString("0.0.131.0.4.255"))) {  // DST working mode
            boolean dsEnabled = protocol.getCosemObjectFactory().getClock().isDsEnabled();
            return new RegisterValue(register, dsEnabled ? "DST switching enabled." : "DST switching disabled.");
        } else if (obisCode.equals(ObisCode.fromString("0.0.131.0.5.255"))) {    // DST flag
            return new RegisterValue(register, Integer.toString(protocol.getDstFlag()));
        } else if (obisCode.equals(ObisCode.fromString("0.0.131.0.6.255")) | obisCode.equals(ObisCode.fromString("0.0.131.0.7.255"))) {    // DST switching times
            return getDSTSwitchingTime(register);
        }

        // *********************************************************************************
        // All other registers
        if (obisCode.getF() == 255) {
            final UniversalObject uo = protocol.getMeterConfig().findObject(obisCode);
            if (uo.getClassID() == DLMSClassId.REGISTER.getClassId()) {
                com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
                return new RegisterValue(register, cosemRegister.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                final DemandRegister demandRegister = cof.getDemandRegister(obisCode);
                return new RegisterValue(register, demandRegister.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                final ExtendedRegister extendedRegister = cof.getExtendedRegister(obisCode);
                return new RegisterValue(register, extendedRegister.getQuantityValue());
            } else if (uo.getClassID() == DLMSClassId.DATA.getClassId()) {
                final Data data = cof.getData(obisCode);
                VisibleString visibleString = data.getValueAttr().getVisibleString();
                if (visibleString != null && visibleString.getStr() != null) {
                    return new RegisterValue(register, visibleString.getStr());
                }
                OctetString octetString = data.getValueAttr().getOctetString();
                if (octetString != null && octetString.stringValue() != null) {
                    return new RegisterValue(register, octetString.stringValue());
                }
            } else if (uo.getClassID() == DLMSClassId.REGISTER_MONITOR.getClassId()) {
                RegisterMonitor registerMonitor = cof.getRegisterMonitor(obisCode);
                int value = registerMonitor.readThresholds().getDataType(0).intValue();
                return new RegisterValue(register, new Quantity(value, Unit.getUndefined()));
            }
        }

        // *********************************************************************************
        // billing registers & all others not yet read out
        CosemObject cosemObject = cof.getCosemObject(obisCode);

        if (cosemObject == null) {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }

        Date captureTime = null;
        Date billingDate = null;
        String text = null;
        Quantity quantityValue = null;

        try {
            captureTime = cosemObject.getCaptureTime();
        } catch (Exception e) {
        }
        try {
            billingDate = cosemObject.getBillingDate();
        } catch (Exception e) {
        }
        try {
            quantityValue = cosemObject.getQuantityValue();
        } catch (Exception e) {
        }
        try {
            text = cosemObject.getText();
        } catch (Exception e) {
        }

        registerValue = new RegisterValue(register, quantityValue,
                captureTime == null ? billingDate : captureTime, null,
                billingDate, new Date(), 0, text
        );

        return registerValue;
    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

    private RegisterValue getDSTSwitchingTime(Register register) throws IOException {
        ObisCode baseObis = ObisCode.fromString("0.0.131.0.6.255");

        Clock clock = protocol.getCosemObjectFactory().getClock();
        byte[] dsDate;
        if (register.getObisCode().equals(baseObis)) {
            dsDate = clock.getDsDateTimeBegin();
        } else {
            dsDate = clock.getDsDateTimeEnd();
        }

        int year = (((dsDate[0] & 0xFF) << 8) | (dsDate[1] & 0xFF));
        String text = "";

        text += (year == 0xFFFF || year == 0x090C) ? "Year: *" : "Year: " + year;
        text += " - ";
        text += (dsDate[2] == (byte) 0xFF) ? "month: *" : "month: " + dsDate[2];
        text += " - ";

        if (dsDate[3] == (byte) 0xFF) {
            text += "day of month: *";
        } else if (dsDate[3] == (byte) 0xFE) {
            text += "day of month: last day of month";
        } else if (dsDate[3] == (byte) 0xFD) {
            text += "day of month: 2th last day of month";
        } else {
            text += "day of month: " + dsDate[3];
        }
        text += " - ";
        text += (dsDate[4] == (byte) 0xFF) ? "day of week: *" : "day of week: " + dsDate[4];
        text += " - ";
        text += "hour: " + dsDate[5];
        return new RegisterValue(register, text);
    }

    private Date getDateTime(byte[] responseData) throws IOException {
        Calendar gcalendarMeter = null;
        gcalendarMeter = buildCalendar(responseData);
        return new Date(gcalendarMeter.getTime().getTime());
    }

    private Calendar buildCalendar(byte[] responseData) throws IOException {
        Calendar gcalendarMeter = null;

        int status = (int) responseData[13] & 0xFF;
        if (status != 0xFF) {
            gcalendarMeter = ProtocolUtils.initCalendar((responseData[13] & (byte) 0x80) == (byte) 0x80, protocol.getTimeZone());
        } else {
            gcalendarMeter = ProtocolUtils.getCleanCalendar(protocol.getTimeZone());
        }

        int year = (int) ProtocolUtils.getShort(responseData, 2) & 0x0000FFFF;
        if (year != 0xFFFF) {
            gcalendarMeter.set(Calendar.YEAR, year);
        }

        int month = (int) responseData[4] & 0xFF;
        if (month != 0xFF) {
            gcalendarMeter.set(Calendar.MONTH, month - 1);
        }

        int date = (int) responseData[5] & 0xFF;
        if (date != 0xFF) {
            gcalendarMeter.set(Calendar.DAY_OF_MONTH, date);
        }

        int hour = (int) responseData[7] & 0xFF;
        if (hour != 0xFF) {
            gcalendarMeter.set(Calendar.HOUR_OF_DAY, hour);
        }

        int minute = (int) responseData[8] & 0xFF;
        if (minute != 0xFF) {
            gcalendarMeter.set(Calendar.MINUTE, minute);
        }

        int seconds = (int) responseData[9] & 0xFF;
        if (seconds != 0xFF) {
            gcalendarMeter.set(Calendar.SECOND, seconds);
        }

        gcalendarMeter.set(Calendar.MILLISECOND, 0);

        return gcalendarMeter;
    }
}