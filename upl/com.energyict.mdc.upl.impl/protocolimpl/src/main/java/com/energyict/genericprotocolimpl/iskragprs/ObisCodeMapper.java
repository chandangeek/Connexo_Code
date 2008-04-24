/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.genericprotocolimpl.iskragprs;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.NoSuchElementException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {
    
    CosemObjectFactory cof[] = {new CosemObjectFactory(null), new CosemObjectFactory(null)};
    RegisterProfile registerProfile[] = {new RegisterProfile(), new RegisterProfile()};
    
    private static final int DAILY 		= 	0x00;
    private static final int MONTHLY	=	0x01;
    
    private static ObisCode dailyObisCode 		= null;
    private static ObisCode monthlyObisCode 	= null;
    private	static ArrayList profileConfiguration = null;
    
    public int billingIndex;
    private boolean debug 			= false;
    private boolean daily			= false;
    private boolean monthly			= false;
    
    
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CosemObjectFactory cof) {
        this.cof[DAILY]		=	cof;
        this.cof[MONTHLY]	=	cof;
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
//            // obis F code
//        	if(obisCode.getF() == 0){
//        		if(debug)System.out.println("A Daily billing register");
//        		billingPoint = -2;
//        	}
//        	
//        	else if (obisCode.getF() == -1){
//        		if(debug)System.out.println("A Monthly billing register");
//        		billingPoint = -2;
//        	}
//        		
//        	else if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
        	if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
                billingPoint = obisCode.getF()+101;
            else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
                billingPoint = (obisCode.getF()*-1)+101;
            else if ((obisCode.getF()  <=101) && (obisCode.getF() < 255))
                billingPoint = obisCode.getF();
            else if (obisCode.getF() == 255)
                billingPoint = -1;
            else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            
            if ( (billingPoint != -1) && (billingPoint != -2) )
                obisCode = new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),billingPoint);
            
            
            //*********************************************************************************
            //	Daily and Monthly registers
            if ( daily || monthly ) {
            	int cofIndex = -1;
            	Date billingDate;
            	Date fromTime;
            	
            	if (daily) cofIndex = 0;
            	else if (monthly) cofIndex = 1;
            	
            	CosemObject cosemObject = null;
            	
            	switch(cofIndex){
            	case 0:{
            		registerProfile[DAILY].setCosemObjectFactory(cof[DAILY]);
            		registerProfile[DAILY].setProfileConfiguration(profileConfiguration);
            		registerProfile[DAILY].getInterval(dailyObisCode);
            		registerProfile[DAILY].getProfileBuffer(dailyObisCode);
            		cosemObject = registerProfile[DAILY].getValues(obisCode);
            		
            	}break;
            		
            	case 1:{
            		registerProfile[MONTHLY].setCosemObjectFactory(cof[MONTHLY]);
            		registerProfile[MONTHLY].setProfileConfiguration(profileConfiguration);
            		registerProfile[MONTHLY].getProfileBuffer(monthlyObisCode);
            		cosemObject = registerProfile[MONTHLY].getValues(obisCode);
            		
            	}break;
            		
            	default: throw new NoSuchElementException("Only daily and monthly register reads.");
            	}
            	if(cosemObject != null){
                	billingDate = cosemObject.getBillingDate();
                	registerValue = new RegisterValue(obisCode,cosemObject.getQuantityValue(),null,billingDate);
                    return registerValue; 
            	}
            	return null;
            }

            // *********************************************************************************
            // General purpose ObisRegisters & abstract general service
            if ((obisCode.toString().indexOf("1.1.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.0.255") != -1)) { // billing counter
                Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,0,255));
                registerValue = new RegisterValue(obisCode,data.getQuantityValue());
                return registerValue;
            } // billing counter
            else if ((obisCode.toString().indexOf("1.1.0.1.1.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.1.255") != -1)) { // nr of available billing periods
                Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,1,255));
                registerValue = new RegisterValue(obisCode,data.getQuantityValue());
                return registerValue;
            } // billing counter
            else if ((obisCode.toString().indexOf("1.1.0.1.2.") != -1) || (obisCode.toString().indexOf("1.0.0.1.2.") != -1)) { // billing point timestamp
                if (billingPoint >= 101) {
                    Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                    registerValue = new RegisterValue(obisCode,data.getBillingDate());
                    return registerValue;
                } else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            } // // billing point timestamp
            
            // *********************************************************************************
            // Abstract ObisRegisters
            if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {
                
	            CosemObject cosemObject = cof[DAILY].getCosemObject(obisCode);
	            
	            if (cosemObject==null)
	                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!"); 
	            
	            if ( (obisCode.toString().indexOf("0.0.128.30.21.255") != -1) ) { // Disconnector
                    registerValue = new RegisterValue(obisCode,
                            cosemObject.getQuantityValue(),
                            null, null, null,
                            new Date(),0,
                            cosemObject.getText());
                    return registerValue; 
	            }
	            
	            else if (( obisCode.toString().indexOf("0.0.128.7.") != -1) || (obisCode.toString().indexOf("0.0.128.8.") != -1)){
	            	registerValue = new RegisterValue(obisCode,cosemObject.getQuantityValue());
	            	return registerValue;
	            }
	            
	            else
	            	throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

//                Date captureTime = null;
//                Date billingDate = null;
//                try {
//                   captureTime = cosemObject.getCaptureTime();
//                   billingDate = cosemObject.getBillingDate();
//                }
//                catch(ClassCastException e) {
//                    // absorb
//                }
//                try {
//                    registerValue = new RegisterValue(obisCode,
//                                                      cosemObject.getQuantityValue(),
//                                                      captureTime==null?billingDate:captureTime,
//                                                      null,
//                                                      billingDate,
//                                                      new Date(),
//                                                      0,
//                                                      cosemObject.getText());
//                    return registerValue;      
//                }
//                catch(ClassCastException e) {
//                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//                }
            }
            
            
            // *********************************************************************************
            // Electricity related ObisRegisters
            if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() <= 2))) {
                if (obisCode.getD() == 8) { // cumulative values, indexes
                    Register register = cof[DAILY].getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()));
                    }
                } // if (obisCode.getD() == 8) { // cumulative values, indexes
                else if (obisCode.getD() == 4) { // current average
                    Register register = cof[DAILY].getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()));
                    }
                } // if (obisCode.getD() == 4) { // current average
                else if (obisCode.getD() == 5) { // last average
                    Register register = cof[DAILY].getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()));
                    }
                } // if (obisCode.getD() == 5) { // last average
                else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister register = cof[DAILY].getExtendedRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),register.getCaptureTime(),data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getUnit()),register.getCaptureTime());
                    }
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {
            
            
            // MBus register
            if (obisCode.getC() == 128) {
            	if ( (obisCode.getD() == 50) && (obisCode.getE() == 0) ){
            		ExtendedRegister register = cof[DAILY].getExtendedRegister(obisCode);
            		Data data = cof[DAILY].getData(new ObisCode(0,1,128,50,0,255));
            		BigDecimal am = BigDecimal.valueOf(register.getValue());
            		Unit u = null;
            		if (register.getScalerUnit().getUnitCode() != 0)
            			u = register.getScalerUnit().getUnit();
            		else u = Unit.get(BaseUnit.UNITLESS, 0);
            		
            		Date captime = register.getCaptureTime();
            		return new RegisterValue(obisCode, new Quantity(am, u), null, captime);
            	}
            }
            
        } catch(IOException e) {
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        
    } // private Object doGetRegister(ObisCode obisCode) throws IOException

	public static void setDailyObisCode(ObisCode dailyObisCode) {
		ObisCodeMapper.dailyObisCode = dailyObisCode;
	}

	public static void setMonthlyObisCode(ObisCode monthlyObisCode) {
		ObisCodeMapper.monthlyObisCode = monthlyObisCode;
	}

	public void setDaily() {
		daily = true;
	}

	public void setMonthly() {
		monthly = true;
	}

	public void clearDaily() {
		daily = false;
	}

	public void clearMonthly() {
		monthly = false;
	}

	public static void setProfileConfiguration(ArrayList profileConfiguration) {
		ObisCodeMapper.profileConfiguration = profileConfiguration;
	}
    
} // public class ObisCodeMapper

