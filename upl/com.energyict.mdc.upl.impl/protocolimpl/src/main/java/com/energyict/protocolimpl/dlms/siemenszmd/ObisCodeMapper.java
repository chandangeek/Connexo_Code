/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.protocolimpl.dlms.siemenszmd;

import java.math.BigInteger;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.dlms.DLMSZMD;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    private final CosemObjectFactory cof;
    private final DLMSMeterConfig meterConfig;
    private final DLMSZMD protocol;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public ObisCodeMapper(final CosemObjectFactory cof, final DLMSMeterConfig meterConfig, final DLMSZMD protocol) {
        this.cof = cof;
        this.meterConfig = meterConfig;
        this.protocol = protocol;
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode);
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {

        RegisterValue registerValue = null;
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
            registerValue = new RegisterValue(obisCode, cof.getCosemObject(ObisCode.fromString("1.0.0.1.0.255")).getQuantityValue());
            return registerValue;
        } // billing counter
        else if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                registerValue = new RegisterValue(obisCode,
                        cof.getStoredValues().getBillingPointTimeDate(billingPoint));
                return registerValue;
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) {  // but we will read the 1.0.0.0.0.255   -   SerialNumber
            return new RegisterValue(obisCode, this.protocol.getSerialNumber());
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.5.255"))) {  // but we will read the 1.0.0.2.0.255   -   FirmwareVersion
            return new RegisterValue(obisCode, this.protocol.getFirmwareVersion());
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.4.255"))) {  // but we will read the 0.0.96.2.0.255  -   Program Counter
            return new RegisterValue(obisCode, new Quantity(new BigInteger(String.valueOf(this.protocol.requestConfigurationProgramChanges())), Unit.getUndefined()));
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {    // Battery usage counter
            Register register = cof.getRegister(obisCode);
            return new RegisterValue(obisCode, com.energyict.genericprotocolimpl.common.ParseUtils.registerToQuantity(register));
        } else if (obisCode.equals(ObisCode.fromString("0.0.13.0.0.255"))) {    // Activity Calendar Name
            return new RegisterValue(obisCode, null, null, null, null, new Date(), 0,
                    new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().getOctetStr()));
        } else if (obisCode.equals(ObisCode.fromString("0.0.97.97.0.255"))) {   // Error status
            Register register = cof.getRegister(obisCode);
            String errorRegister = convertToEightDigitErrorRegister(Long.toHexString(register.getValue()).toUpperCase());
            return new RegisterValue(obisCode, errorRegister);
        }
        // *********************************************************************************
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

        registerValue = new RegisterValue(obisCode, quantityValue,
                captureTime == null ? billingDate : captureTime, null,
                billingDate, new Date(), 0, text
        );

        return registerValue;

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

    /**
     * Create an 8digit errorRegisterString
     *
     * @param s the Hex-value of the errorString.
     * @return
     */
    protected String convertToEightDigitErrorRegister(final String s) {
        String errorRegister = "00000000";
        errorRegister = errorRegister.substring(0, errorRegister.length() - s.length()).concat(s);
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < errorRegister.length(); i++) {
            if(i != 0 && i%2 == 0){
                strBuilder.append(" ");
            }
            strBuilder.append(errorRegister.charAt(i));
        }
        return strBuilder.toString();
    }

} // public class ObisCodeMapper 
