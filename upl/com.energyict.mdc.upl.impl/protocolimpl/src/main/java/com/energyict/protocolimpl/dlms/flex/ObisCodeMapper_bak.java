/*
 * ObisCodeMapper.java
 * 
 * Created on 08 januari 2008
 *
 */

package com.energyict.protocolimpl.dlms.flex;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

/**
 *
 * @author gna
 */
public class ObisCodeMapper_bak {
    
    CosemObjectFactory cof;
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper_bak(CosemObjectFactory cof) {
        this.cof=cof;
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode);
    }
    
    private Object doGetRegister(ObisCode obisCode) throws IOException {
        
        RegisterValue registerValue=null;
        int billingPoint=-1;
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
            else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            
            if (billingPoint != -1)
                obisCode = new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),billingPoint);
            
            // *********************************************************************************
            // General purpose ObisRegisters & abstract general service
            if ((obisCode.toString().indexOf("1.1.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.0.255") != -1)) { // billing counter
                Data data = cof.getData(new ObisCode(1,0,0,1,0,255));
                registerValue = new RegisterValue(obisCode,data.getQuantityValue());
                return registerValue;
            } // billing counter
            else if ((obisCode.toString().indexOf("1.1.0.1.1.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.1.255") != -1)) { // nr of available billing periods
                Data data = cof.getData(new ObisCode(1,0,0,1,1,255));
                registerValue = new RegisterValue(obisCode,data.getQuantityValue());
                return registerValue;
            } // billing counter
            else if ((obisCode.toString().indexOf("1.1.0.1.2.") != -1) || (obisCode.toString().indexOf("1.0.0.1.2.") != -1)) { // billing point timestamp
                if (billingPoint >= 101) {
                    Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                    registerValue = new RegisterValue(obisCode,data.getBillingDate());
                    return registerValue;
                } else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            } // // billing point timestamp
            
            // *********************************************************************************
            // Abstract ObisRegisters
            if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {
                
            CosemObject cosemObject = cof.getCosemObject(obisCode);
            
            if (cosemObject==null)
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!"); 

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
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()));
                    }
                } // if (obisCode.getD() == 8) { // cumulative values, indexes
                else if (obisCode.getD() == 4) { // current average
                    Register register = cof.getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()));
                    }
                } // if (obisCode.getD() == 4) { // current average
                else if (obisCode.getD() == 5) { // last average
                    Register register = cof.getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()));
                    }
                } // if (obisCode.getD() == 5) { // last average
                else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister register = cof.getExtendedRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof.getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),register.getCaptureTime(),data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),register.getCaptureTime());
                    }
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {
        } catch(IOException e) {
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        
    } // private Object doGetRegister(ObisCode obisCode) throws IOException
    
} // public class ObisCodeMapper

