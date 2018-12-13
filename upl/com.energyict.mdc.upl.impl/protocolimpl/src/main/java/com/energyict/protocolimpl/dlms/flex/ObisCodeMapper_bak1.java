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
import com.energyict.dlms.cosem.Register;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
*
* @author  Koen
*  <B>@beginchanges</B><BR>
	GN|10012008|Taken over everything from the Iskra protocol and adapted where needed
* @endchanges
*/
public class ObisCodeMapper_bak1 {

    CosemObjectFactory cof;
    public int billingIndex;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper_bak1(CosemObjectFactory cof) {
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
        ObisCode newObisCode = obisCode;
        int billingPoint;
        try {

            if ((obisCode.getF()  >=0) && (obisCode.getF() <= 99))
                billingPoint = obisCode.getF()+101;
            else if ((obisCode.getF()  <=0) && (obisCode.getF() >= -99))
                billingPoint = (obisCode.getF()*-1)+101;
            else if ((obisCode.getF()  >=101) && (obisCode.getF() <= 125))
                billingPoint = obisCode.getF();
            else if (obisCode.getF() == 255)
                billingPoint = -1;
            else if ( obisCode.getF() == 126 ){
//            	billingPoint = obisCode.getF();
            	throw new NoSuchRegisterException("Billing Value Profile with obiscode " + obisCode.toString() + " not yet supported");
            }
            else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }

            if (billingPoint != -1) {
                newObisCode = new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), billingPoint);
            }


            // *********************************************************************************
            // BillingPoint registers
            if ( (billingPoint != -1) ) {

            	billingIndex = ( billingPoint - 101 ) + 1;

                CosemObject cosemObject = cof.getStoredValues().getHistoricalValue(newObisCode);

                Date billingDate = cosemObject.getBillingDate();
                registerValue = new RegisterValue(obisCode,
                                                  cosemObject.getQuantityValue(),
                                                  billingDate);
                return registerValue;
            }


            // *********************************************************************************
            // Abstract ObisRegisters
            if ((obisCode.getA() == 0) && (obisCode.getB() == 0)) {
                CosemObject cosemObject = cof.getCosemObject(obisCode);

                if (cosemObject==null) {
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
						billingDate, new Date(), 0, text);

				return registerValue;
            }


            // *********************************************************************************
            // Electricity related ObisRegisters
            if ((obisCode.getA() == 1) && (obisCode.getB() == 1)) {
                Register register = cof.getRegister(obisCode);
                if (obisCode.getD() == 8 || obisCode.getD() == 7) { // cumulative values, indexes
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(register.getValue()),register.getScalerUnit().getEisUnit()));
                } // if (obisCode.getD() == 8) { // cumulative values, indexes

            } // if ((obisCode.getA() == 1) && (obisCode.getB() == 1)) {
        } catch (IOException e) {
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    } // private Object doGetRegister(ObisCode obisCode) throws IOException

} // public class ObisCodeMapper
