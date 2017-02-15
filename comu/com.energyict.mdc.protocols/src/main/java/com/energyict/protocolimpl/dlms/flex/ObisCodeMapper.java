/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.flex;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.dlms.cosem.Register;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.NoSuchElementException;


/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {

    CosemObjectFactory cof[] = {new CosemObjectFactory(null), new CosemObjectFactory(null)};
    RegisterProfile registerProfile[] = {new RegisterProfile(), new RegisterProfile()};

    private static final int DAILY 		= 	0x00;
    private static final int MONTHLY	=	0x01;

    // It is this one for Founter!!
//    private static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
//  private static final ObisCode monthlyObisCode = ObisCode.fromString("1.0.98.2.0.255");

    // These are for the ESSENT project, make them compatible later!
    private static final ObisCode dailyObisCode = ObisCode.fromString("1.0.98.2.0.255");
    private static final ObisCode monthlyObisCode = ObisCode.fromString("1.0.98.1.0.255");



    public int billingIndex;
    private boolean debug = false;



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
            // obis F code
        	if(obisCode.getF() == 0){
        		if(debug)System.out.println("A Daily billing register");
        		billingPoint = -2;
        	}

        	else if (obisCode.getF() == -1){
        		if(debug)System.out.println("A Monthly billing register");
        		billingPoint = -2;
        	}

        	else if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
//        	if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
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


            // -------------------------------------------------------------------------------------------
            //BillingPoint registers
            if ( (billingPoint == -2) ) {
            	int cofIndex = -1;
            	Date billingDate;
            	Date fromTime;

            	billingIndex = obisCode.getF();

            	if (billingIndex == 0) cofIndex = 0;
            	else if (billingIndex == -1) cofIndex = 1;

            	billingPoint = 255;
            	CosemObject cosemObject = null;

            	switch(cofIndex){
            	case 0:{
            		registerProfile[DAILY].setCosemObjectFactory(cof[DAILY]);
            		registerProfile[DAILY].getInterval(dailyObisCode);
            		registerProfile[DAILY].getProfileBuffer(dailyObisCode);
            		cosemObject = registerProfile[DAILY].getValues(obisCode);

            		billingDate = cosemObject.getBillingDate();
            		fromTime = registerProfile[DAILY].getFromDate();

            	}break;

            	case 1:{
            		registerProfile[MONTHLY].setCosemObjectFactory(cof[MONTHLY]);
            		registerProfile[MONTHLY].getProfileBuffer(monthlyObisCode);
            		cosemObject = registerProfile[MONTHLY].getValues(obisCode);

            		billingDate = cosemObject.getBillingDate();
            		fromTime = registerProfile[MONTHLY].getFromDate();

            	}break;

            	default: throw new NoSuchElementException("Only daily and monthly register reads.");
            	}

//            	Date billingDate =

//            	obisCode = new ObisCode(obisCode.getA(),obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingIndex);

//                CosemObject cosemObject = cof[cofIndex].getStoredValues().getHistoricalValue(obisCode);
//
//                DataContainer dc = cof.getProfileGeneric(ObisCode.fromString("1.0.98.1.0.255")).getBuffer();
//

//                registerValue = new RegisterValue(obisCode,
//                                                  cosemObject.getQuantityValue(),
//                                                  billingDate, fromTime, billingDate, billingDate);

            	registerValue = new RegisterValue(obisCode,cosemObject.getQuantityValue(),null,billingDate);
                return registerValue;
            }
            // -------------------------------------------------------------------------------------------


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

                Date captureTime = null;
                Date billingDate = null;
                String text = null;
                Quantity quantityValue = null;

                try {captureTime = cosemObject.getCaptureTime();} catch (Exception e) {}
				try {billingDate = cosemObject.getBillingDate();} catch (Exception e) {}
				try {quantityValue = cosemObject.getQuantityValue();} catch (Exception e) {}
				try {text = cosemObject.getText();} catch (Exception e) {}

				try {
					registerValue = new RegisterValue(
							obisCode, quantityValue,
							captureTime == null ? billingDate : captureTime,
							null, billingDate,
							new Date(), 0, text
					);

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
            if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() >= 2))) {
                if (obisCode.getD() == 8) { // cumulative values, indexes
                    Register register = cof[DAILY].getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                    }
                } // if (obisCode.getD() == 8) { // cumulative values, indexes
                else if (obisCode.getD() == 4) { // current average
                    Register register = cof[DAILY].getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                    }
                } // if (obisCode.getD() == 4) { // current average
                else if (obisCode.getD() == 5) { // last average
                    Register register = cof[DAILY].getRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),null,data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                    }
                } // if (obisCode.getD() == 5) { // last average
                else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister register = cof[DAILY].getExtendedRegister(obisCode);
                    if (billingPoint != -1) {
                        // get Billing timestamp
                        Data data = cof[DAILY].getData(new ObisCode(1,0,0,1,2,billingPoint));
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),register.getCaptureTime(),data.getBillingDate());
                    } else {
                        return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()),register.getCaptureTime());
                    }
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {

            if (obisCode.getC() == 128) {
            	if ( (obisCode.getD() == 50) && (obisCode.getE() == 0) ){
            		ExtendedRegister register = cof[DAILY].getExtendedRegister(obisCode);
            		Data data = cof[DAILY].getData(new ObisCode(0,1,128,50,0,255));
            		BigDecimal am = BigDecimal.valueOf(register.getValue());
            		Unit u = null;
            		if (register.getScalerUnit().getUnitCode() != 0)
            			u = register.getScalerUnit().getEisUnit();
            		else u = Unit.get(BaseUnit.UNITLESS, 0);

            		Date captime = register.getCaptureTime();
            		return new RegisterValue(obisCode, new Quantity(am, u), captime);
//            		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),),register.getCaptureTime());
//            		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(0),Unit.get(BaseUnit.CUBICMETER)));
            	}
            }

        } catch(IOException e) {
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    } // private Object doGetRegister(ObisCode obisCode) throws IOException

	private void test(CosemObject co) {


	}

} // public class ObisCodeMapper