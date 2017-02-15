/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.MT83RegisterConfig;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.MT83Registry;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.DateValuePair;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author jme
 */
public class MT83ObisCodeMapper {

    private static final int DEBUG = 0;
    private MT83Registry mt83Registry = null;
    private MT83RegisterConfig regs = null;
    private DataDumpParser dataDumpParser;
    private TimeZone timeZone;
    private boolean dataReadoutEnabled;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public MT83ObisCodeMapper(final MT83Registry mt83Registry, final TimeZone timeZone, final MT83RegisterConfig regs, final boolean dataReadoutEnabled) {
        this.mt83Registry = mt83Registry;
        this.timeZone = timeZone;
        this.regs = regs;
        this.dataReadoutEnabled = dataReadoutEnabled;
    }

    public MT83RegisterConfig getMT83RegisterConfig() {
        return regs;
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        ObisCode obis = getMT83RegisterConfig().obisToDeviceCode(obisCode);
        RegisterValue regVal = (RegisterValue) doGetRegister(obis);

        RegisterValue returnValue = new RegisterValue(
                obisCode,
                regVal.getQuantity(),
                regVal.getEventTime(),
                regVal.getFromTime(),
                regVal.getToTime(),
                regVal.getReadTime(),
                regVal.getRegisterSpecId(),
                regVal.getText()
        );

        return returnValue;
    }

    private int getBillingResetCounter() throws IOException {
        return ((Integer) mt83Registry.getRegister(MT83Registry.BILLING_RESET_COUNTER)).intValue();
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {
        RegisterValue registerValue;
        int billingPoint;
        DateValuePair dvp;

        // obis F code
        if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
            billingPoint = obisCode.getF();
        } else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
            billingPoint = obisCode.getF() * -1;
        } else if (obisCode.getF() == 255) {
            billingPoint = -1;
        } else {
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported! (1) ");
        }

        String billingDateRegister;
        Date eventDate = null;
        Date fromDate = null;
        Date toDate = null;

        String strReg = null;
        strReg = obisCode.toString();

        try {

            // First do all the abstract Objects
            if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) { // just read the serialNumber
                String value = (String) mt83Registry.getRegister(MT83Registry.SERIAL);
                return new RegisterValue(obisCode, value);
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.5.255"))) { // just read the firmwareVersion
                String fwversion = "";
                fwversion += "Version: " + mt83Registry.getRegister(MT83Registry.SOFTWARE_REVISION) + " - ";
                fwversion += "Device date: " + mt83Registry.getRegister(MT83Registry.SOFTWARE_DATE) + " - ";
                fwversion += "Device Type: " + mt83Registry.getRegister(MT83Registry.DEVICE_TYPE);
                return new RegisterValue(obisCode, fwversion);
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {    // just read the battery status in hours
                String batteryHours = (String) mt83Registry.getRegister(MT83Registry.BATTERY_HOURS);
                batteryHours = batteryHours.replace(",", ".");
                return new RegisterValue(obisCode, new Quantity(batteryHours, Unit.get(BaseUnit.HOUR)));
            } else if (obisCode.equals(ObisCode.fromString("1.1.0.1.0.255"))) { // billing reset counter
                return new RegisterValue(obisCode, new Quantity(new BigDecimal(getBillingResetCounter()), Unit.getUndefined()));
            } else if (obisCode.equals(ObisCode.fromString("1.1.0.1.2.255"))) { // billing reset counter
                Date startDate = ((Date) mt83Registry.getRegister(MT83Registry.BILLING_DATE_1));
                return new RegisterValue(obisCode, startDate);
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.4.255"))) {
                Date lastConfigDate = (Date) mt83Registry.getRegister(MT83Registry.LAST_CONFIG_CHANGE_DATE);
                return new RegisterValue(obisCode, lastConfigDate);
            }

            if (billingPoint != -1) {

                ObisCode billingStartObis = ObisCode.fromString(MT83Registry.BILLING_DATE_START);
                billingDateRegister = new ObisCode(billingStartObis.getA(), billingStartObis.getB(), billingStartObis.getC(), billingStartObis.getD(), billingStartObis.getE(), billingStartObis.getF() + ObisCode.fromString(strReg).getF()).toString();

                MT83.sendDebug("Reading toDate from obis: " + billingDateRegister.toString(), DEBUG);

                if (this.dataReadoutEnabled) {
                    try {
                        String dateStr = this.dataDumpParser.getRegisterStrValue(toEdis(obisCode));
                        if (dateStr != null && !dateStr.isEmpty()) {
                            toDate = ProtocolUtils.parseDateTimeWithTimeZone(dateStr.substring(dateStr.lastIndexOf("(") + 1, dateStr.lastIndexOf(")")), "yyMMddHHmmss", timeZone);
                        } else {
                            toDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                        }
                    } catch (ParseException e) {
                        toDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                    } catch (NoSuchRegisterException e) {
                        toDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                    }
                } else {
                    toDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                }

                eventDate = toDate;

                billingDateRegister = new ObisCode(billingStartObis.getA(), billingStartObis.getB(), billingStartObis.getC(), billingStartObis.getD(), billingStartObis.getE(), billingStartObis.getF() + (ObisCode.fromString(strReg).getF() + 1)).toString();

                MT83.sendDebug("Reading fromDate from obis: " + billingDateRegister.toString() + "\n", DEBUG);

                try {

                    if (this.dataReadoutEnabled) {
                        try {
                            ObisCode previous = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), obisCode.getF() + 1);
                            String dateStr = this.dataDumpParser.getRegisterStrValue(toEdis(previous));
                            if (dateStr != null && !dateStr.isEmpty()) {
                                fromDate = ProtocolUtils.parseDateTimeWithTimeZone(dateStr.substring(dateStr.lastIndexOf("(") + 1, dateStr.lastIndexOf(")")), "yyMMddHHmmss", timeZone);
                            } else {
                                fromDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                            }
                        } catch (ParseException e) {
                            fromDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                        } catch (NoSuchRegisterException e) {
                            fromDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                        }
                    } else {
                        fromDate = ((Date) mt83Registry.getRegister(billingDateRegister));
                    }

                } catch (Exception e) {
                    MT83.sendDebug("fromDate does not exist. [" + billingDateRegister + "]", DEBUG);
                    fromDate = null;
                }

            }


            MT83.sendDebug("Reading register: Obis = " + obisCode.toString() + " Edis: " + obisCode, 0);

            if (this.dataReadoutEnabled) {

                try {
                    Quantity quantity = this.dataDumpParser.getRegister(toEdis(obisCode));
                    return new RegisterValue(obisCode, quantity, eventDate, fromDate, toDate, new Date(), 0);
                } catch (NoSuchRegisterException e) {
                    // Will try to read it normally
                }
            }

            dvp = (DateValuePair) mt83Registry.getRegister(strReg + " DATE_VALUE_PAIR");

            Unit obisCodeUnit = dvp.getUnit();
            if (!obisCode.getUnitElectricity(0).getBaseUnit().toString().equalsIgnoreCase(obisCodeUnit.getBaseUnit().toString())) {
                if (!obisCodeUnit.isUndefined()) {
                    throw new NoSuchRegisterException("Unit of the obiscode (" + obisCode.getUnitElectricity(0).getBaseUnit() + ") doesn't match the unit of the register received from the meter (" + obisCodeUnit.getBaseUnit() + ")");
                }
                obisCodeUnit = obisCode.getUnitElectricity(0);
                if (obisCodeUnit.getDlmsCode() != 255) {
                    obisCodeUnit = obisCode.getUnitElectricity(regs.getScaler());
                }
            }


            Quantity quantity = new Quantity(dvp.getValue(), obisCodeUnit);

            if (quantity.getAmount() != null) {
                eventDate = dvp.getDate();
                if (eventDate == null) {
                    eventDate = toDate;
                }
                registerValue = new RegisterValue(obisCode, quantity, eventDate, fromDate, toDate, new Date(), 0);
            } else {
                String strValue = (String) mt83Registry.getRegister(strReg + " STRING");
                registerValue = new RegisterValue(obisCode, strValue);
            }
            return registerValue;

        }
        catch (IOException e) {
            MT83.sendDebug(e.getMessage(), DEBUG);
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported! (3) " + e.toString());
        }
    }

    public void setDataDumpParser(final DataDumpParser dataDumpParser) {
        this.dataDumpParser = dataDumpParser;
    }

    /* Convert Obis code to Edis code. */

    private String toEdis(ObisCode obis) throws IOException {
        return obis.getC() + "." + obis.getD() + "." + obis.getE() + getEdisBillingNotation(obis);
    }

    private String getEdisBillingNotation(ObisCode obis) throws IOException {
        if (obis.getF() != 255) {
            return "*" + ProtocolUtils.buildStringDecimal(obis.getF(), 2);
        } else {
            return "";
        }
    }
}
