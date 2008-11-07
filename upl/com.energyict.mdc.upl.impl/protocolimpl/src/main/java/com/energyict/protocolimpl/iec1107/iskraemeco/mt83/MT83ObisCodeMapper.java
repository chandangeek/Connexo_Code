/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

import java.util.*;
import java.io.*;
import java.math.BigDecimal;

import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.cbo.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig.*;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.*;

/**
 *
 * @author  Koen
 */
public class MT83ObisCodeMapper {
    private static final int DEBUG = 1;
    
    private static final Date JAN2008 = new Date(946681200000L);
	
	MT83Registry iskraEmecoRegistry;
    RegisterConfig regs;
    
    /** Creates a new instance of ObisCodeMapper */
    public MT83ObisCodeMapper(MT83Registry iskraEmecoRegistry, TimeZone timeZone, RegisterConfig regs) {
        this.iskraEmecoRegistry=iskraEmecoRegistry;
        this.regs=regs;
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        MT83ObisCodeMapper ocm = new MT83ObisCodeMapper(null,null,null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode,true);
    }
    
    private int getBillingResetCounter(RegisterConfig regs) throws IOException {
        //String strReg = regs.getMeterRegisterCode(ObisCode.fromString("1.0.0.1.0.255"));
    	//return ((Integer)iskraEmecoRegistry.getRegister(strReg+" INTEGER")).intValue();
    	return ((Integer)iskraEmecoRegistry.getRegister(MT83Registry.BILLING_RESET_COUNTER)).intValue();
    }
    
    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        RegisterValue registerValue=null;
        int billingPoint=-1;
        
        String billingDateRegister;
        Date eventDate = null;
        Date fromDate = null;
        Date toDate = null;
        
        // obis F code
        if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
            billingPoint = obisCode.getF();
        else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99))
            billingPoint = obisCode.getF()*-1;
        else if (obisCode.getF() == 255)
            billingPoint = -1;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (1) ");
        
        ObisCode oc = obisCode;
        //ObisCode oc = new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),255);
        
        
        if (read) {
            String strReg=null;
            strReg = obisCode.toString();

            if (strReg == null) 
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (2) ");
            try {
                
                if (billingPoint != -1) {
                    int VZ = getBillingResetCounter(regs);                    
                    //strReg = strReg+"*"+(VZ-billingPoint);
                    MT83.sendDebug("doGetregister(): Billingpoint = " + billingPoint + " - getBillingResetCounter = " + VZ + " - strReg = " + strReg, DEBUG);

                    ObisCode billingStartObis = ObisCode.fromString(MT83Registry.BILLING_DATE_START);
                    billingDateRegister = new ObisCode(billingStartObis.getA(), billingStartObis.getB(), billingStartObis.getC(), billingStartObis.getD(), billingStartObis.getE(), billingStartObis.getF() + obisCode.getF()).toString();
                    toDate = ((Date)iskraEmecoRegistry.getRegister(billingDateRegister.toString()));

                    if (obisCode.getF() > 1) {
                        billingDateRegister = new ObisCode(billingStartObis.getA(), billingStartObis.getB(), billingStartObis.getC(), billingStartObis.getD(), billingStartObis.getE(), billingStartObis.getF() + (obisCode.getF() - 1)).toString();
                        fromDate = ((Date)iskraEmecoRegistry.getRegister(billingDateRegister.toString()));
                    }
                    
                    if (toDate.equals(JAN2008) || fromDate.equals(JAN2008)) {
                    	fromDate = null;
                    	toDate = null;
                    }
                }
                
                DateValuePair dvp = (DateValuePair)iskraEmecoRegistry.getRegister(strReg+" DATE_VALUE_PAIR");
                Unit obisCodeUnit = dvp.getUnit(); 
                if (!obisCode.getUnitElectricity(0).getBaseUnit().toString().equalsIgnoreCase(obisCodeUnit.getBaseUnit().toString())) {
                	if (!obisCodeUnit.isUndefined()) throw new NoSuchRegisterException("Unit of the obiscode (" + obisCode.getUnitElectricity(0).getBaseUnit() +") doesn't match the unit of the register received from the meter (" + obisCodeUnit.getBaseUnit() + ")");
                	obisCodeUnit = obisCode.getUnitElectricity(0);
                	if (obisCodeUnit.getDlmsCode() != 255)
                        obisCodeUnit = obisCode.getUnitElectricity(regs.getScaler());
                }
                
                Quantity quantity = new Quantity(dvp.getValue(),obisCodeUnit);
                if (quantity.getAmount() != null) {
                	eventDate = dvp.getDate();
                	registerValue = new RegisterValue(obisCode, quantity, eventDate, fromDate, toDate);
                } else {
                   String strValue = (String)iskraEmecoRegistry.getRegister(strReg+" STRING");
                   registerValue = new RegisterValue(obisCode,strValue);
                }
                return registerValue;
            }
            catch(IOException e) {
                MT83.sendDebug(e.getMessage(), DEBUG);
            	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! (3) "+e.toString());
            }
        }
        else {
            return new RegisterInfo(obisCode.getDescription());
        }
        
    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException      
    
} // public class ObisCodeMapper
