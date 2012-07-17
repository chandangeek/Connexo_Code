/*
 * ObisCodeMapper.java
 *
 * Created on 17 augustus 2004, 9:21
 */

package com.energyict.protocolimpl.dlms.iskrame37x;

import com.energyict.cbo.*;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
/**
 *
 * @author  Koen
 */
public class ObisCodeMapper {

    static ObisCode billingProfileObiscodeMonthly = ObisCode.fromString("1.0.98.1.0.255");
    static ObisCode billingProfileObiscodeDaily = ObisCode.fromString("1.0.98.2.0.255");

    CosemObjectFactory cof;
    StoredValuesImpl[] storedValues;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(CosemObjectFactory cof) throws IOException {
        this.cof = cof;
        storedValues = new StoredValuesImpl[] {new StoredValuesImpl(cof, billingProfileObiscodeMonthly), new StoredValuesImpl(cof, billingProfileObiscodeDaily)};
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode);
    }

    private Object doGetRegister(ObisCode obisCode) throws IOException {
        
        RegisterValue registerValue;
        try {
            // *********************************************************************************
            //Billing registers
            if (obisCode.getF() != 255) {
                int absBillingPoint = Math.abs(obisCode.getF());
                int billingPoint = absBillingPoint > 11 ? absBillingPoint - 12 : absBillingPoint;

                if ((obisCode.toString().indexOf("1.0.0.1.2.") != -1) || (obisCode.toString().indexOf("1.1.0.1.2.") != -1)) { // billing point timestamp
                    Date billingPointTimeDate = getStoredValues(obisCode).getBillingPointTimeDate(billingPoint);
                    registerValue = new RegisterValue(obisCode, billingPointTimeDate);
                    return registerValue;
                } else {
                    ObisCode obis = ProtocolTools.setObisCodeField(obisCode, 5, (byte) billingPoint);
                    HistoricalValue historicalValue = getStoredValues(obisCode).getHistoricalValue(obis);
                    return new RegisterValue(obisCode, historicalValue.getQuantityValue(), historicalValue.getEventTime(), historicalValue.getBillingDate());
                }
            }
            // ---------------------------------------------------------------------------------

            
            // *********************************************************************************
            // General purpose ObisRegisters & abstract general service
            if ((obisCode.toString().indexOf("1.1.0.1.0.255") != -1) || (obisCode.toString().indexOf("1.0.0.1.0.255") != -1)) { // billing counter
                Data data = cof.getData(new ObisCode(1, 0, 0, 1, 0, 255));
                Unsigned16 counter = (Unsigned16) data.getValueAttr();
                registerValue = new RegisterValue(obisCode, new Quantity(counter.toBigDecimal(), Unit.getUndefined()));
                return registerValue;
            } // billing counter

            // *********************************************************************************
            if (obisCode.toString().indexOf("1.0.0.1.1.255") != -1) { // nr of available monthly billing periods
                int counter = storedValues[0].getBillingPointCounter();
                registerValue = new RegisterValue(obisCode,new Quantity(counter, Unit.getUndefined()));
                return registerValue;
            } // billing counter

            if (obisCode.toString().indexOf("1.1.0.1.1.255") != -1) { // nr of available daily billing periods
                int counter = storedValues[1].getBillingPointCounter();
                registerValue = new RegisterValue(obisCode,new Quantity(counter, Unit.getUndefined()));
                return registerValue;
            } // billing counter


            // *********************************************************************************
            // Abstract ObisRegisters
            if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {
	            CosemObject cosemObject = cof.getCosemObject(obisCode);
	            
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
            }

            // *********************************************************************************
            // Electricity related ObisRegisters
            if ((obisCode.getA() == 1) && ((obisCode.getB() == 0) || (obisCode.getB() >= 2))) {
                if (obisCode.getD() == 8) { // cumulative values, indexes
                    Register register = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 8) { // cumulative values, indexes
                else if (obisCode.getD() == 4) { // current average
                    Register register = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 4) { // current average
                else if (obisCode.getD() == 5) { // last average
                    Register register = cof.getRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 5) { // last average
                else if (obisCode.getD() == 6) { // maximum demand values
                    ExtendedRegister register = cof.getExtendedRegister(obisCode);
                    return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), register.getScalerUnit().getEisUnit()), register.getCaptureTime());
                } // maximum demand values
            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {

            if (obisCode.getC() == 128) {
                if ((obisCode.getD() == 50) && (obisCode.getE() == 0)) {
                    ExtendedRegister register = cof.getExtendedRegister(obisCode);
                    BigDecimal am = BigDecimal.valueOf(register.getValue());
                    Unit u;
                    if (register.getScalerUnit().getUnitCode() != 0) {
                        u = register.getScalerUnit().getEisUnit();
                    } else {
                        u = Unit.get(BaseUnit.UNITLESS, 0);
                    }

                    Date captime = register.getCaptureTime();
                    return new RegisterValue(obisCode, new Quantity(am, u), captime);
                }
            }

        } catch (IOException e) {
        }
        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");

    } // private Object doGetRegister(ObisCode obisCode) throws IOException

    private StoredValuesImpl getStoredValues(ObisCode obisCode) {
        if (Math.abs(obisCode.getF()) < 12) {
            return storedValues[0];
        } else {
            return storedValues[1];
        }
    }
} // public class ObisCodeMapper

