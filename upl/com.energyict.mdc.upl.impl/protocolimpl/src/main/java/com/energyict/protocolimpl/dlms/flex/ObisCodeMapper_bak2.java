/*
 * ObisCodeMapper.java
 *
 * Created on 08 januari 2008
 *
 */

package com.energyict.protocolimpl.dlms.flex;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author gna
 */
public class ObisCodeMapper_bak2 {

    CosemObjectFactory cof;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper_bak2(CosemObjectFactory cof) {
        this.cof=cof;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) {
        return new RegisterInfo(obisCode.toString());
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode);
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {

        RegisterValue registerValue;
        int billingPoint;
        try {
            // obis F code
            if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
                billingPoint = obisCode.getF()+101;
            else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
                billingPoint = (obisCode.getF()*-1)+101;
            else if ((obisCode.getF()  <=101) && (obisCode.getF() < 255))
                billingPoint = obisCode.getF();
            else if (obisCode.getF() == 255)
                billingPoint = -1;
            else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }

            if (billingPoint != -1) {
                obisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingPoint);
            }

            // *********************************************************************************
            // General purpose ObisRegisters & abstract general service
            if ((obisCode.toString().contains("1.1.0.1.0.255")) || (obisCode.toString().contains("1.0.0.1.0.255"))) { // billing counter
                Data data = cof.getData(new ObisCode(1,0,0,1,0,255));
                registerValue = new RegisterValue(obisCode,data.getQuantityValue());
                return registerValue;
            } // billing counter
            else if ((obisCode.toString().contains("1.1.0.1.1.255")) || (obisCode.toString().contains("1.0.0.1.1.255"))) { // nr of available billing periods
                Data data = cof.getData(new ObisCode(1,0,0,1,1,255));
                registerValue = new RegisterValue(obisCode,data.getQuantityValue());
                return registerValue;
            } // billing counter
            else if ((obisCode.toString().contains("1.1.0.1.2.")) || (obisCode.toString().contains("1.0.0.1.2."))) { // billing point timestamp
                if (billingPoint >= 101) {
                    Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                    registerValue = new RegisterValue(obisCode,data.getBillingDate());
                    return registerValue;
                } else {
                    throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
                }
            } // // billing point timestamp

            // *********************************************************************************
            // Abstract ObisRegisters
            if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {

            CosemObject cosemObject = cof.getCosemObject(obisCode);

            if (cosemObject==null) {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }

                Date captureTime = null;
                Date billingDate = null;
                try {
                   captureTime = cosemObject.getCaptureTime();
                   billingDate = cosemObject.getBillingDate();
                }
                catch(ClassCastException e) {
                    // absorb
                }
                try {
                    registerValue = new RegisterValue(obisCode,
                                                      cosemObject.getQuantityValue(),
                                                      captureTime==null?billingDate:captureTime,
                                                      null,
                                                      billingDate,
                                                      new Date(),
                                                      0,
                                                      cosemObject.getText());
                    return registerValue;
                }
                catch(ClassCastException e) {
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
                }
//                CosemObject cosemObject = cof.getCosemObject(obisCode);
//                if (cosemObject==null)
//                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//                return new RegisterValue(obisCode,cosemObject.getText());
            }


            // *********************************************************************************
            // Electricity related ObisRegisters
            if ((obisCode.getA() == 1) && ((obisCode.getB() == 1) || (obisCode.getB() >= 2))) {
                if (obisCode.getD() == 8) { // cumulative values, indexes
                    Register register = cof.getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                    }
                } // if (obisCode.getD() == 8) { // cumulative values, indexes
                else if (obisCode.getD() == 4) { // current average
                    Register register = cof.getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                    }
                } // if (obisCode.getD() == 4) { // current average
                else if (obisCode.getD() == 5) { // last average
                    Register register = cof.getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                    }
                } // if (obisCode.getD() == 5) { // last average
                else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister register = cof.getExtendedRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),register.getCaptureTime(),data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),register.getCaptureTime());
                    }
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {
        } catch (IOException e) {
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    } // private Object doGetRegister(ObisCode obisCode) throws IOException

} // public class ObisCodeMapper
