/*
 * ObisCodeMapper.java
 *
 * Created on 15 juli 2004, 8:49
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;

import java.io.IOException;
import java.util.TimeZone;
/**
 *
 * @author gna
 * <B>@Beginchanges</B><BR>
 * GN|28012008| created obisCodeNumber to map the spec numbers with the OBIS codes
 * @Endchanges
 */
public class ObisCodeMapper {

	private static final int TARIFF_HV = 128;
	private static final int TARIFF_HP = 129;
	private static final int TARIFF_HC = 130;
	private static final int TARIFF_HSV = 131;
	private static final int TARIFF_HFV = 132;

    //TimeZone timeZone;
    DataReadingCommandFactory drcf;
    RegisterConfig regs;
    RegisterSetFactory rsf;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(DataReadingCommandFactory drcf, TimeZone timeZone, RegisterConfig regs) {
        //this.timeZone=timeZone;
        this.drcf=drcf;
        this.regs=regs;
        this.rsf = new RegisterSetFactory(drcf);
//        if (this.rsf == null) rsf = new RegisterSetFactory(drcf);
    }

    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null,null,null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode,true);
    }

    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
    	ObisCode newObisCode = obisCode;
        RegisterValue registerValue=null;
        String registerName=null;
        Unit unit = null;
        int billingPoint=-1;
        int registerNumber;

        // obis F code
        if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
            billingPoint = obisCode.getF();
        else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
            billingPoint = obisCode.getF()*-1;
        else if (obisCode.getF() == 255)
            billingPoint = 0;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

        if (billingPoint != 0)
        	newObisCode = new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),billingPoint);

        if (read){

        	// this reads the all billingPoints at once, just for this period
            rsf.getRegisterSet(billingPoint);
            registerNumber = obisCodeNumber(obisCode);

            if (rsf.registersets[billingPoint] != null)

            	if ( registerNumber == 99 )
            		registerValue = new RegisterValue(obisCode, rsf.getRegisterSet(billingPoint).getRegister(registerNumber).toString());

            	else
            		registerValue = new RegisterValue(obisCode, rsf.getRegisterSet(billingPoint).getRegister(registerNumber).quantity,null,
            				rsf.getRegisterSet(billingPoint).getRegister(registerNumber).billingTimestamp);

            else throw new NoSuchRegisterException("Billingperiod " + billingPoint + " is not yet available.");

            return registerValue;
        }

        else {
            return new RegisterInfo(obisCode.getDescription());
        }

    } // private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException

//    private int obisCodeNumber(ObisCode obisCode) throws NoSuchRegisterException{
//
//    	// instantaneous values
//        if ( obisCode.toString().indexOf("1.0.1.8.0.255") != -1 )
////        	return 14;
//        	return 81;
//        else if ( obisCode.toString().indexOf("1.0.3.8.0.255") != -1 )
////        	return 15;
//        	return 82;
//        else if ( obisCode.toString().indexOf("1.0.4.8.0.255") != -1 )
////        	return 16;
//        	return 83;
//
//        else if ( obisCode.toString().indexOf("1.0.0.0.0.255") != -1 )
//        	return 99;
//
//        // active energy tariffs
//        else if ( obisCode.toString().indexOf("1.0.1.") != -1 ){
//        	if ( obisCode.getD() == 8 ){ //interval values
//        		switch (obisCode.getE()){
//        		case TARIFF_HV: /*return 0;*/ return 67;
//        		case TARIFF_HP: /*return 1;*/ return 68;
//        		case TARIFF_HC: /*return 2;*/ return 69;
//        		case TARIFF_HSV: /*return 3;*/ return 70;
//        		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        		}
//        	}
//        	// maximum demand
//        	else if ( obisCode.getD() == 6 ){ //maximum demands
//        		switch (obisCode.getE()){
//        		case TARIFF_HV: /*return 8;*/ return 75;
//        		case TARIFF_HFV: /*return 9;*/ return 76;
//        		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        		}
//        	}
//        	else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        }
//
//        // reactive energy inductive tariffs
//        else if ( obisCode.toString().indexOf("1.0.3.") != -1 ){
//        	if ( obisCode.getD() == 8 ){
//        		switch (obisCode.getE()){
//        		case TARIFF_HV: /*return 4;*/ return 71;
//        		case TARIFF_HFV: /*return 5;*/ return 72;
//        		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        		}
//        	}
//        	// maximum demand
//        	else if ( obisCode.getD() == 6 ){
//        		switch (obisCode.getE()){
//        		case TARIFF_HV: /*return 10;*/ return 77;
//        		case TARIFF_HFV: /*return 11;*/ return 78;
//        		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        		}
//        	}
//        	else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported");
//        }
//
//        // reactive energy capacitive tariffs
//        else if ( obisCode.toString().indexOf("1.0.4.") != -1 ){
//        	if ( obisCode.getD() == 8 ){
//        		switch (obisCode.getE()){
//        		case TARIFF_HV: /*return 6;*/ return 73;
//        		case TARIFF_HFV: /*return 7;*/ return 74;
//        		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        		}
//        	}
//        	// maximum demand
//        	else if ( obisCode.getD() == 6 ){
//        		switch (obisCode.getE()){
//        		case TARIFF_HV: /*return 12;*/ return 79;
//        		case TARIFF_HFV: /*return 13;*/ return 80;
//        		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//        		}
//        	}
//        	else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported");
//        }
//
//        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//
//    }

    private int obisCodeNumber(ObisCode obisCode) throws NoSuchRegisterException{

    	if ( obisCode.toString().indexOf("1.0.0.0.0.255") != -1 )
    		return 99;

    	if(obisCode.getA() == 1){
    		if(obisCode.getC() == 1){
    			if ( obisCode.getD() == 8 ){ //interval values
    				if((obisCode.getE() == 0) &&(obisCode.getF() == 255))
    					return 81;
    				switch (obisCode.getE()){
    				case TARIFF_HV: /*return 0;*/ return 67;
    				case TARIFF_HP: /*return 1;*/ return 68;
    				case TARIFF_HC: /*return 2;*/ return 69;
    				case TARIFF_HSV: /*return 3;*/ return 70;
    				default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    				}
    			}
    			// maximum demand
    			else if ( obisCode.getD() == 6 ){ //maximum demands
    				switch (obisCode.getE()){
    				case TARIFF_HV: /*return 8;*/ return 75;
    				case TARIFF_HFV: /*return 9;*/ return 76;
    				default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    				}
    			}
    			else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    		}

    		if(obisCode.getC() == 3){
            	if ( obisCode.getD() == 8 ){
    				if((obisCode.getE() == 0) &&(obisCode.getF() == 255))
    					return 82;
            		switch (obisCode.getE()){
            		case TARIFF_HV: /*return 4;*/ return 71;
            		case TARIFF_HFV: /*return 5;*/ return 72;
            		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            		}
            	}
            	// maximum demand
            	else if ( obisCode.getD() == 6 ){
            		switch (obisCode.getE()){
            		case TARIFF_HV: /*return 10;*/ return 77;
            		case TARIFF_HFV: /*return 11;*/ return 78;
            		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            		}
            	}
            	else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported");
    		}

    		if(obisCode.getC() == 4){
    			if ( obisCode.getD() == 8 ){
    				if((obisCode.getE() == 0) &&(obisCode.getF() == 255))
    					return 83;
            		switch (obisCode.getE()){
            		case TARIFF_HV: /*return 6;*/ return 73;
            		case TARIFF_HFV: /*return 7;*/ return 74;
            		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            		}
            	}
            	// maximum demand
            	else if ( obisCode.getD() == 6 ){
            		switch (obisCode.getE()){
            		case TARIFF_HV: /*return 12;*/ return 79;
            		case TARIFF_HFV: /*return 13;*/ return 80;
            		default: throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            		}
            	}
            	else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported");
    		}

    		else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported");
    	}
    	else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

} // public class ObisCodeMapper
