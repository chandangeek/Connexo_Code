/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.protocolimpl.dlms.eictz3;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    private final CosemObjectFactory cof;
    private final RegisterProfileMapper registerProfileMapper;

    public ObisCodeMapper(CosemObjectFactory cof) {
        this.cof = cof;
        registerProfileMapper = new RegisterProfileMapper(cof);
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue) doGetRegister(obisCode);
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {

        RegisterValue registerValue;
        int billingPoint;

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
        if ((obisCode.toString().contains("1.1.0.1.0.255")) || (obisCode.toString().contains("1.0.0.1.0.255"))) { // billing counter
            registerValue = new RegisterValue(obisCode, new Quantity(new Integer(cof.getStoredValues().getBillingPointCounter()), Unit.get("")));
            return registerValue;
        } // billing counter
        else if ((obisCode.toString().contains("1.1.0.1.2.")) || (obisCode.toString().contains("1.0.0.1.2."))) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
                registerValue = new RegisterValue(obisCode,
                        cof.getStoredValues().getBillingPointTimeDate(billingPoint));
                return registerValue;
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } // // billing point timestamp

        // *********************************************************************************
        // Abstract ObisRegisters
        if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {
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
        }


        // *********************************************************************************
        // Electricity related ObisRegisters
        if ((obisCode.getA() == 1) && (obisCode.getB() == 1)) {
            CosemObject cosemObject = null;
            if (obisCode.getF() != 255) {
                ObisCode profileObisCode = registerProfileMapper.getMDProfileObisCode(obisCode);
                if (profileObisCode == null) {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
                }
                cosemObject = cof.getStoredValues().getHistoricalValue(profileObisCode);
            } else {
                cosemObject = registerProfileMapper.getRegister(obisCode);
            }

            if (cosemObject == null) {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }


            Date captureTime = cosemObject.getCaptureTime();
            Date billingDate = cosemObject.getBillingDate();
            registerValue = new RegisterValue(obisCode,
                    cosemObject.getQuantityValue(),
                    captureTime == null ? billingDate : captureTime,
                    null,
                    cosemObject.getBillingDate(),
                    new Date(),
                    0,
                    cosemObject.getText());
            return registerValue;
        }

        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
    }

}