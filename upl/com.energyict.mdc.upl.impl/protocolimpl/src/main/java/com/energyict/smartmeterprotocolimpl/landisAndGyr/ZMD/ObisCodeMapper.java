package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigInteger;
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

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
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
            return new RegisterValue(register, com.energyict.genericprotocolimpl.common.ParseUtils.registerToQuantity(cosemRegister));
        } else if (obisCode.equals(ObisCode.fromString("0.0.13.0.0.255"))) {    // Activity Calendar Name
            return new RegisterValue(register, null, null, null, null, new Date(), 0,
                    new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().getOctetStr()));
        } else if (obisCode.equals(ObisCode.fromString("0.0.97.97.0.255"))) {   // Error status
            com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
            String errorRegister = ProtocolUtils.outputHexString(cosemRegister.getValueAttr().getOctetString().getOctetStr());
            return new RegisterValue(register, errorRegister);
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

        registerValue = new RegisterValue(register, quantityValue,
                captureTime == null ? billingDate : captureTime, null,
                billingDate, new Date(), 0, text
        );

        return registerValue;

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException
}