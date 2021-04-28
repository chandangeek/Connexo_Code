/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.protocolimpl.dlms.actarisace6000;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.generic.ParseUtils;

import java.io.IOException;
import java.util.Date;

/**
 * @author Koen
 */
public class ObisCodeMapper {

    private CosemObjectFactory cof;
    private RegisterProfileMapper registerProfileMapper = null;

    public static final String ACTIVITY_CALENDAR_NAME = "0.0.13.0.0.255";
    public static final String VOLTAGE_L1 = "1.1.32.7.0.255";
    public static final String VOLTAGE_L2 = "1.1.52.7.0.255";
    public static final String VOLTAGE_L3 = "1.1.72.7.0.255";
    public static final String CURRENT_L1 = "1.1.31.7.0.255";
    public static final String CURRENT_L2 = "1.1.51.7.0.255";
    public static final String CURRENT_L3 = "1.1.71.7.0.255";
    private final ACE6000 protocol;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CosemObjectFactory cof, final ACE6000 protocol) {
        this.cof=cof;
        registerProfileMapper = new RegisterProfileMapper(cof);
        this.protocol = protocol;

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
        try {
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
        } catch (NoSuchRegisterException | NotInObjectListException e) {
            // Absorb the exception and continue.
            // This indicates the register should be mapped to a registerProfile.
        }

        // *********************************************************************************
        // Electricity related ObisRegisters mapped to a registerProfile
        if ((obisCode.getA() == 1) && (obisCode.getB() == 1)) {
            CosemObject cosemObject;
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
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) {
            return new RegisterValue(obisCode, protocol.getMeterSerialNumber());
        } else if (obisCode.equals(ObisCode.fromString("0.0.96.1.5.255"))) {
            return new RegisterValue(obisCode, this.protocol.getFirmwareVersion());
        }else if (obisCode.equals(ObisCode.fromString(ACTIVITY_CALENDAR_NAME))) {    // Activity Calendar Name
            return new RegisterValue(obisCode, null, null, null, null, new Date(), 0,
                    new String(cof.getActivityCalendar(obisCode).readCalendarNameActive().getOctetStr()));
        }else if (obisCode.equals(ObisCode.fromString(VOLTAGE_L1))||
                obisCode.equals(ObisCode.fromString(VOLTAGE_L2))||
                obisCode.equals(ObisCode.fromString(VOLTAGE_L3)) ||
                obisCode.equals(ObisCode.fromString(CURRENT_L1))  ||
                obisCode.equals(ObisCode.fromString(CURRENT_L2)) ||
                obisCode.equals(ObisCode.fromString(CURRENT_L3))) {    //CURRENT_L3
            com.energyict.dlms.cosem.Register cosemRegister = cof.getRegister(obisCode);
            return new RegisterValue(obisCode, ParseUtils.registerToQuantity(cosemRegister));
        }

        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
/*
        // obis C code
        if ((obisCode.getC() == 1) || (obisCode.getC() == 2) || (obisCode.getC() == 5) ||
            (obisCode.getC() == 6) || (obisCode.getC() == 7) || (obisCode.getC() == 8)) {
            // *************************************************************************************************************
            // C U M U L A T I V E  M A X I M U M  D E M A N D (OBIC D field 'Cumulative maximum 1' DLMS UA 1000-1 ed.5 page 87/101)
            if ((obisCode.getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) && (obisCode.getB() == 1)) {
                CosemObject cosemObject = cof.getCosemObject(obisCode);
                registerValue = new RegisterValue(obisCode,
                                                 cosemObject.getQuantityValue(),
                                                 cosemObject.getBillingDate(),
                                                 cosemObject.getBillingDate());
                return registerValue;
            }
            // *************************************************************************************************************
            // R I S I N G  D E M A N D (OBIC D field 'Current average 1' DLMS UA 1000-1 ed.5 page 87/101)
            else if ((obisCode.getD() == ObisCode.CODE_D_RISING_DEMAND) && (obisCode.getB() == 1)) {
                CosemObject cosemObject = cof.getCosemObject(obisCode);
                registerValue = new RegisterValue(obisCode,
                                                 cosemObject.getQuantityValue(),
                                                 cosemObject.getBillingDate(),
                                                 cosemObject.getBillingDate());
                return registerValue;
            }
            // *************************************************************************************************************
            // R I S I N G  D E M A N D (OBIC D field 'Current average 1' DLMS UA 1000-1 ed.5 page 87/101)
            else if ((obisCode.getD() == ObisCode.CODE_D_RISING_DEMAND) && (obisCode.getB() == 1)) {
                CosemObject cosemObject = cof.getCosemObject(obisCode);
                registerValue = new RegisterValue(obisCode,
                                                 cosemObject.getQuantityValue(),
                                                 cosemObject.getBillingDate(),
                                                 cosemObject.getBillingDate());
                return registerValue;
            }
            // *************************************************************************************************************
            // M A X I M U M  D E M A N D (OBIC D field 'Maximum 1' DLMS UA 1000-1 ed.5 page 87/101)
            else if ((obisCode.getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) && (obisCode.getB() == 1)) {
                CosemObject cosemObject = cof.getCosemObject(obisCode);
                registerValue = new RegisterValue(obisCode,
                                                 cosemObject.getQuantityValue(),
                                                 cosemObject.getBillingDate(),
                                                 cosemObject.getBillingDate());
                return registerValue;
            }
            // *************************************************************************************************************
            // T O T A L & R A T E (OBIC D field 'Time integral 1' DLMS UA 1000-1 ed.5 page 87/101)
            else if (obisCode.getD() == ObisCode.CODE_D_TIME_INTEGRAL) {// time integral 1 TOTAL & RATE
                CosemObject cosemObject = cof.getCosemObject(obisCode);
                registerValue = new RegisterValue(obisCode,
                                                 cosemObject.getQuantityValue(),
                                                 cosemObject.getBillingDate(),
                                                 cosemObject.getBillingDate());
                return registerValue;
            }
            else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        }
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
  */

    }
}
