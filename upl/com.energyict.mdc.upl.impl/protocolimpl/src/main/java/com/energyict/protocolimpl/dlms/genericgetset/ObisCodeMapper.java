/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.protocolimpl.dlms.genericgetset;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;

import com.energyict.obis.*;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {
    
    CosemObjectFactory cof;
    RegisterProfileMapper registerProfileMapper=null;
    
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CosemObjectFactory cof) {
        this.cof=cof;
        registerProfileMapper = new RegisterProfileMapper(cof);
        
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
        
        // obis F code
        if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
            billingPoint = obisCode.getF();
        else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
            billingPoint = obisCode.getF()*-1;
        else if (obisCode.getF() == 255)
            billingPoint = -1;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        
        // ********************************************************************************* 
        // General purpose ObisRegisters & abstract general service
        if ((obisCode.toString().indexOf("1.1.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.0.255") != -1)) { // billing counter
            registerValue = new RegisterValue(obisCode,new Quantity(new Integer(cof.getStoredValues().getBillingPointCounter()),Unit.get(""))); 
            return registerValue;
        } // billing counter
        else if ((obisCode.toString().indexOf("1.1.0.1.2.") != -1) || (obisCode.toString().indexOf("1.0.0.1.2.") != -1)) { // billing point timestamp
            if ((billingPoint >= 0) && (billingPoint < 99)) {
               registerValue = new RegisterValue(obisCode,
                                                 cof.getStoredValues().getBillingPointTimeDate(billingPoint)); 
               return registerValue;
            }
            else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        } // // billing point timestamp
      
        // *********************************************************************************
        // Abstract ObisRegisters
        if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {
            CosemObject cosemObject = cof.getCosemObject(obisCode);
            
            if (cosemObject==null)
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!"); 

            Date captureTime = cosemObject.getCaptureTime();
            Date billingDate = cosemObject.getBillingDate();
            registerValue = new RegisterValue(obisCode,
                                              cosemObject.getQuantityValue(),
                                              captureTime==null?billingDate:captureTime,
                                              null,
                                              cosemObject.getBillingDate(),
                                              new Date(),
                                              0,
                                              cosemObject.getText());
            return registerValue;
        }
        

        // *********************************************************************************
        // Electricity related ObisRegisters
        if ((obisCode.getA() == 1) && (obisCode.getB() == 1)) {
            CosemObject cosemObject=null;
            if (obisCode.getF() != 255) {
                ObisCode profileObisCode = registerProfileMapper.getMDProfileObisCode(obisCode);
                if (profileObisCode==null)
                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!"); 
                cosemObject = cof.getStoredValues().getHistoricalValue(profileObisCode);
            }
            else cosemObject = registerProfileMapper.getRegister(obisCode);
            
            if (cosemObject==null)
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!"); 
            
            
            Date captureTime = cosemObject.getCaptureTime();
            Date billingDate = cosemObject.getBillingDate();
            registerValue = new RegisterValue(obisCode,
                                              cosemObject.getQuantityValue(),
                                              captureTime==null?billingDate:captureTime,
                                              null,
                                              cosemObject.getBillingDate(),
                                              new Date(),
                                              0,
                                              cosemObject.getText());
            return registerValue;
        }
        
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");  
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
